package com.example.studentrecord.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Practical AOP example: cross-cutting logging for every service method.
 *
 * <p>Without AOP, each service would need repeated timing and exception-logging
 * code. The pointcut below applies the advice around all public service
 * methods, so the logging concern stays in one place.</p>
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(public * com.example.studentrecord.service..*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        log.info("--> {}", method);
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("<-- {} completed in {} ms", method, elapsed);
            return result;
        } catch (Throwable throwable) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("!!  {} failed after {} ms: {}", method, elapsed, throwable.getMessage());
            throw throwable;
        }
    }

    @AfterThrowing(pointcut = "execution(public * com.example.studentrecord.api..*(..))", throwing = "exception")
    public void logApiFailure(Throwable exception) {
        log.error("REST API exception: {}", exception.getMessage());
    }
}
