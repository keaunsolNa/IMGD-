package com.nks.imgd.component.util.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nks
 * @apiNote Method 실행 전 후 추적을 위한 AOP
 */
@Aspect
@Configuration
@Slf4j
public class MethodAOP {

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

}
