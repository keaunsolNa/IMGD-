package com.nks.imgd.component.util.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

		Annotation[][] anns = method.getParameterAnnotations();

		if (args.length == 1) {
			Object single = args[0];

			// 1) 단일 파라미터에 @Param 붙어있으면 그 이름으로 래핑
			String paramName = null;
			if (anns.length > 0) {
				for (Annotation a : anns[0]) {
					if (a instanceof Param) {
						paramName = ((Param) a).value();
						break;
					}
				}
			}
			if (paramName != null) {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put(paramName, single);  // 예: "holi" -> DTO
				m.put("param1", single);
				m.put("value", single);
				m.put("_parameter", single);
				return m;
			}

			// 2) @Param 없고 단순 타입이면 value/param1로 별칭 제공
			if (isSimpleType(single)) {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("value", single);
				m.put("param1", single);
				m.put("_parameter", single);
				return m;
			}

			// 3) 그 외(POJO 한 개)는 그대로 전달 (#{field} 형태로 사용할 수 있음)
			return single;
		}

		// 다중 파라미터: param1..N + @Param 이름 모두 제공
		Map<String, Object> paramMap = new LinkedHashMap<>();
		for (int i = 0; i < args.length; i++) paramMap.put("param" + (i + 1), args[i]);
		for (int i = 0; i < anns.length; i++) {
			for (Annotation a : anns[i]) {
				if (a instanceof Param) paramMap.put(((Param) a).value(), args[i]);
			}
		}
		// MyBatis 관례 키
		paramMap.put("_parameter", paramMap);
		return paramMap;
	}


	/**
	 * BoundSql의 '?'를 실제 리터럴로 치환 (XML의 개행/들여쓰기 유지)
	 * @param cfg 설정
	 * @param boundSql SQL 쿼리 문
	 * @return 치환된 쿼리문
	 */
	private String renderSqlWithParams(Configuration cfg, BoundSql boundSql) {

		String sql = boundSql.getSql();
		List<ParameterMapping> mappings = boundSql.getParameterMappings();
		if (mappings == null || mappings.isEmpty()) return sql;

		TypeHandlerRegistry registry = cfg.getTypeHandlerRegistry();

		Object paramObj = boundSql.getParameterObject();
		MetaObject metaObject;

		if (paramObj == null) {
			metaObject = null;
		} else if (paramObj instanceof Map<?, ?>) {
			// ParamMap 그대로 사용 (복사/타입 변환 X)
			metaObject = cfg.newMetaObject(paramObj);
		} else if (registry.hasTypeHandler(paramObj.getClass())) {
			// 단일-단순 파라미터일 때만 별칭 제공
			Map<String, Object> alias = new LinkedHashMap<>();
			alias.put("value", paramObj);
			alias.put("param1", paramObj);
			metaObject = cfg.newMetaObject(alias);
		} else {
			metaObject = cfg.newMetaObject(paramObj);
		}

		for (ParameterMapping pm : mappings) {
			String prop = pm.getProperty();
			Object value = null;

			// 1) 추가 파라미터 먼저
			if (boundSql.hasAdditionalParameter(prop)) {
				value = boundSql.getAdditionalParameter(prop);
			}

			// 2) 메인 파라미터에서 직통 조회 시도 (점/인덱스 포함)
			if (value == null && metaObject != null) {
				try {
					value = metaObject.getValue(prop); // hasGetter 검사 없이 바로 시도
				} catch (Exception ignore) {
					// 계속 진행
				}
			}

			// 3) base 토큰이 additionalParameter에 있을 때 (foreach, list[0].field 등)
			if (value == null && prop != null) {
				int split = indexOfFirstDotOrBracket(prop); // 최초의 '.' 또는 '[' 위치
				if (split > 0) {
					String base = prop.substring(0, split);
					String child = prop.substring(split + 1);
					if (boundSql.hasAdditionalParameter(base)) {
						Object addl = boundSql.getAdditionalParameter(base);
						try {
							value = cfg.newMetaObject(addl).getValue(child);
						} catch (Exception ignore) {
							// keep null
						}
					}
				}
			}

			String lit = toSqlLiteral(value);
			sql = sql.replaceFirst("\\?", java.util.regex.Matcher.quoteReplacement(lit));
		}

		return sql;
	}

	/**
	 * Table.Column 형태 대응 위한 처리
	 * @param s 대상 문자열
	 * @return . 혹은 [ 이 존재하는 가장 첫 번째 위치
	 */
	private int indexOfFirstDotOrBracket(String s) {
		int d = s.indexOf('.');
		int b = s.indexOf('[');
		if (d == -1) return b;
		if (b == -1) return d;
		return Math.min(d, b);
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

}
