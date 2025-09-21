package com.mutindo.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for performance logging
 */
@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {

    @Around("@annotation(com.annotation.logging.mutindo.PerformanceLog)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            if (executionTime > 1000) { // Log slow operations (>1s)
                log.warn("SLOW_OPERATION - Class: {}, Method: {}, ExecutionTime: {}ms", 
                        className, methodName, executionTime);
            } else {
                log.debug("PERFORMANCE - Class: {}, Method: {}, ExecutionTime: {}ms", 
                        className, methodName, executionTime);
            }
            
            return result;
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            log.error("PERFORMANCE_ERROR - Class: {}, Method: {}, ExecutionTime: {}ms, Error: {}", 
                    className, methodName, executionTime, throwable.getMessage());
            
            throw throwable;
        }
    }
}
