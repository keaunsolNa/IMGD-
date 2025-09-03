package com.nks.imgd.component.util.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote Query 추적을 위한 AOP
 */
@Aspect
@org.springframework.context.annotation.Configuration
@RequiredArgsConstructor
@Order(0)
public class QueryAOP {

    private static final Logger SQL_LOG = LoggerFactory.getLogger("SQL_LOG");

    private final SqlSessionFactory sqlSessionFactory;

    @Value("${aop.query.enabled}")
    private boolean enabled;

    @Value("${aop.query.print-result}")
    private boolean printResultSummary;

    @Value("${aop.query.truncate-len}")
    private int truncateLen;

    /**
     * Mapper Interface 전역 PointCut
     * @param pjp JoinPoint
     * @return 쿼리문
     * @throws Throwable Exception 발생 시 대응
     */
    @Around("execution(* com.nks.imgd.mapper..*.*(..))")
    public Object logSqlAround(ProceedingJoinPoint pjp) throws Throwable {
        if (!enabled) return pjp.proceed();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        String statementId = sig.getDeclaringTypeName() + "." + sig.getName();
        Object[] args = pjp.getArgs();

        MappedStatement ms;
        try {
            ms = sqlSessionFactory.getConfiguration().getMappedStatement(statementId);
        } catch (Exception e) {
            // Mapper XML에 없는 메서드인 경우(디폴트 메서드 등)
            SQL_LOG.info("[SQL][{}] <no MappedStatement> args={}", statementId, Arrays.toString(args));
            long t0 = System.nanoTime();
            try {
                Object result = pjp.proceed();
                long tookMs = toMs(System.nanoTime() - t0);
                SQL_LOG.info("[SQL][{}] <proceeded> took={}ms {}", statementId, tookMs, summarizeResult(result));
                return result;
            } catch (Throwable ex) {
                SQL_LOG.error("[SQL][{}] threw: {} - {}", statementId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
                throw ex;
            }
        }

        Object paramObject = buildParameterObject(method, args);
        BoundSql boundSql = ms.getBoundSql(paramObject);

        String finalSql = renderSqlWithParams(ms.getConfiguration(), boundSql);

        // 실행 전: XML의 줄바꿈/들여쓰기 유지해서 그대로 출력
        SQL_LOG.info("[SQL][{}]\n{}", statementId, finalSql);

        long t0 = System.nanoTime();
        try {
            Object result = pjp.proceed();
            long tookMs = toMs(System.nanoTime() - t0);
            if (printResultSummary) {
                SQL_LOG.info("[SQL][{}] done in {}ms {}", statementId, tookMs, summarizeResult(result));
            } else {
                SQL_LOG.info("[SQL][{}] done in {}ms", statementId, tookMs);
            }
            return result;
        } catch (Throwable ex) {
            SQL_LOG.error("[SQL][{}] FAILED: {} - {}\n{}",
                    statementId, ex.getClass().getSimpleName(), nullSafe(ex.getMessage()), finalSql, ex);
            throw ex;
        }
    }

    // ───────────────────────────────── helper methods ───────────────────────────────

    /**
     * MyBatis ParamMap 유사 구성: @Param 이름 + param1..N 포함, 단일 값은 value/param1 동시 제공
     * @param method 대상 메서드
     * @param args argument
     * @return Parameter Map
     */
    private Object buildParameterObject(Method method, Object[] args) {
        if (args == null || args.length == 0) return null;
        if (args.length == 1) {
            Object single = args[0];
            // 단일 단순 타입일 때도 반복 사용/중복 바인딩 대응 위해 value/param1 키 제공
            if (isSimpleType(single)) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("value", single);
                m.put("param1", single);
                return m;
            }
            return single;
        }

        Map<String, Object> paramMap = new LinkedHashMap<>();
        // param1..N
        for (int i = 0; i < args.length; i++) paramMap.put("param" + (i + 1), args[i]);
        // @Param 이름 부여
        Annotation[][] anns = method.getParameterAnnotations();
        for (int i = 0; i < anns.length; i++) {
            for (Annotation a : anns[i]) {
                if (a instanceof Param) {
                    paramMap.put(((Param) a).value(), args[i]);
                }
            }
        }
        return paramMap;
    }

    /**
     * BoundSql의 '?'를 실제 리터럴로 치환 (XML의 개행/들여쓰기 유지)
     * @param cfg 설정
     * @param boundSql SQL 쿼리 문
     * @return 치환된 쿼리문
     */
    private String renderSqlWithParams(Configuration cfg, BoundSql boundSql) {

        String sql = boundSql.getSql(); // ★ 공백 압축 금지 → XML 포맷 그대로
        List<ParameterMapping> mappings = boundSql.getParameterMappings();

        if (mappings == null || mappings.isEmpty()) return sql;

        TypeHandlerRegistry registry = cfg.getTypeHandlerRegistry();

        // MetaObject 준비
        Object paramObj = boundSql.getParameterObject();

        MetaObject metaObject;
        Map<String, Object> mapIfAny = null;

        if (paramObj == null) {
            metaObject = null;
        } else if (paramObj instanceof Map<?, ?>) {
            mapIfAny = copyToStringObjectMap((Map<?, ?>) paramObj);   // ★ 여기로 교체
            metaObject = cfg.newMetaObject(mapIfAny);
        } else if (registry.hasTypeHandler(paramObj.getClass())) {
            mapIfAny = new LinkedHashMap<>();
            mapIfAny.put("value", paramObj);
            mapIfAny.put("param1", paramObj);
            metaObject = cfg.newMetaObject(mapIfAny);
        } else {
            metaObject = cfg.newMetaObject(paramObj);
        }

        // 치환 수행
        List<String> usedLiterals = new ArrayList<>();

        for (ParameterMapping pm : mappings) {
            String prop = pm.getProperty();

            Object value;

            if (boundSql.hasAdditionalParameter(prop)) {
                value = boundSql.getAdditionalParameter(prop);
            } else if (metaObject != null && metaObject.hasGetter(prop)) {
                value = metaObject.getValue(prop);
            } else if (prop != null && prop.contains(".")) {
                // foreach에서 생성되는 __frch_*.* 형태 지원
                String base = prop.substring(0, prop.indexOf('.'));
                if (boundSql.hasAdditionalParameter(base)) {
                    Object additional = boundSql.getAdditionalParameter(base);
                    value = cfg.newMetaObject(additional).getValue(prop.substring(base.length() + 1));
                } else {
                    value = null;
                }
            // MetaObject 생성 직후, 단일-단순 파라미터면 모든 매핑키에 alias 주입
            } else if (mapIfAny != null && mapIfAny.containsKey("value")) {
                // 단일 단순 파라미터: XML의 어떤 프로퍼티명이라도 동일 값으로 간주
                Object singleVal = mapIfAny.get("value");
                for (ParameterMapping pmg : mappings) {
                    String p = pmg.getProperty();
                    mapIfAny.putIfAbsent(p, singleVal);
                }

                value = mapIfAny.get("value");
            }
            else {
                value = null;
            }

            String lit = toSqlLiteral(value);
            usedLiterals.add(lit);
            sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(lit));

        }

        // 혹시 남았으면(매핑 누락/중복) 보수적으로 보정:
        // - 모든 리터럴이 동일하고 1종류 뿐이면 남은 '?' 전부 동일 리터럴로 대체
        if (sql.contains("?")) {
            Set<String> uniq = new HashSet<>(usedLiterals);
            if (uniq.size() == 1) {
                String only = usedLiterals.isEmpty() ? "NULL" : usedLiterals.getFirst();
                while (sql.contains("?")) {
                    sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(only));
                }
            }
        }
        return sql;
    }

    /**
     * 타입 확인
     * @param v Parameter Type
     * @return 단순 자료형인지 여부
     */
    private boolean isSimpleType(Object v) {
        return v instanceof CharSequence || v instanceof Number || v instanceof Boolean
                || v instanceof Date || v instanceof TemporalAccessor || v instanceof UUID;
    }

    /**
     * 값 → SQL 리터럴 문자열
     * @param v 값
     * @return SQL 리터럴 문자열
     */
    private String toSqlLiteral(Object v) {
        if (v == null) return "NULL";
        if (v instanceof Boolean || v instanceof Number) return String.valueOf(v);

        if (v instanceof Date) {
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) v) + "'";
        }
        if (v instanceof TemporalAccessor) {
            return "'" + v + "'";
        }
        if (v.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(v);
            List<String> parts = new ArrayList<>(len);
            for (int i = 0; i < len; i++) parts.add(toSqlLiteral(java.lang.reflect.Array.get(v, i)));
            return "(" + String.join(", ", parts) + ")";
        }
        if (v instanceof Collection<?>) {
            List<String> parts = new ArrayList<>();
            for (Object o : (Collection<?>) v) parts.add(toSqlLiteral(o));
            return "(" + String.join(", ", parts) + ")";
        }
        String s = String.valueOf(v);
        if (s.length() > truncateLen) s = s.substring(0, truncateLen) + "...";
        s = s.replace("'", "''"); // escape
        return "'" + s + "'";
    }

    /**
     * 결과 요약
     * @param result Query Statement
     * @return 요약된 Query
     */

    private String summarizeResult(Object result) {
        if (result == null) return "[result=null]";
        if (result instanceof Collection<?>) return "[result=list size=" + ((Collection<?>) result).size() + "]";
        if (result.getClass().isArray()) return "[result=array length=" + java.lang.reflect.Array.getLength(result) + "]";
        if (result instanceof Map<?, ?>) return "[result=map size=" + ((Map<?, ?>) result).size() + "]";
        if (result instanceof Integer || result instanceof Long) return "[result=affected=" + result + "]";
        return "[result=" + result.getClass().getSimpleName() + "]";
    }

    /**
     * 수행 시간 확인 용 메서드
     * @param nanos ms
     * @return long ms
     */
    private long toMs(long nanos) {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    /**
     * 널 타입 확인
     * @param s 대상 string
     * @return null String "" 반환, 아니라면 원문 반환
     */
    private String nullSafe(String s) {
        return null == s ? "" : s;
    }

    /**
     * parameter key 문자열로 셋팅
     * @param src 대상 map
     * @return 타입 변환 Map
     */
    private Map<String, Object> copyToStringObjectMap(Map<?, ?> src) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : src.entrySet()) {
            String key = String.valueOf(e.getKey()); // 키를 문자열로 강제
            out.put(key, e.getValue());
        }
        return out;
    }

}
