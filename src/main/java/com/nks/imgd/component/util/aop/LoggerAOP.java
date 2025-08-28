package com.nks.imgd.component.util.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nks
 * @apiNote Layer 간 추적 위한 AOP
 */
@Aspect
@Configuration
@Slf4j
public class LoggerAOP {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 메서드 실행 전 AOP
	 * @param joinPoint 실행되는 메서드
	 */
	@Before("execution(* com.nks.imgd..*(..))")
	public void logBefore(JoinPoint joinPoint) {
		logger.info("Before {}", joinPoint.getSignature().getName());
	}

	/**
	 * 메서드 실행 후 AOP
	 * @param joinPoint 실행되는 메서드
	 */
	@After("execution(* com.nks.imgd..*(..))")
	public void logAfter(JoinPoint joinPoint) {
		logger.info("After {}", joinPoint.getSignature().getName());
	}

	/**
	 * Exception 발생 시 AOP
	 * @param joinPoint 실행되는 메서드
	 * @param ex Exception
	 */
	@AfterThrowing(
		pointcut = "within(com.nks.imgd..*) || within(com.nks.imgd..*)",
		throwing = "ex"
	)
	public void logException(JoinPoint joinPoint, Throwable ex) {
		String method = joinPoint.getSignature().toShortString();
		String args = Arrays.toString(joinPoint.getArgs());
		logger.error("Exception in {} with args={} -> {}: {}",
			method, args, ex.getClass().getSimpleName(), ex.getMessage(), ex);
	}
}
