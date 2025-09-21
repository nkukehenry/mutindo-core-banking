package com.mutindo.logging.aspect;

import com.mutindo.logging.annotation.PerformanceLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aspect for performance logging
 */
@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(performanceLog)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, PerformanceLog performanceLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String operation = !performanceLog.operation().isEmpty() ? performanceLog.operation() : methodName;
            
            // Use configured threshold or always log if specified
            boolean shouldLog = performanceLog.alwaysLog() || executionTime > performanceLog.threshold();
            
            if (shouldLog) {
                String logLevel = executionTime > performanceLog.threshold() ? "SLOW" : "PERFORMANCE";
                
                String logMessage = String.format("%s_OPERATION - Operation: %s, Class: %s, Method: %s, ExecutionTime: %sms", 
                        logLevel, operation, className, methodName, executionTime);
                
                // Add parameters if configured
                if (performanceLog.logParameters()) {
                    try {
                        logMessage += ", Parameters: " + objectMapper.writeValueAsString(joinPoint.getArgs());
                    } catch (Exception e) {
                        logMessage += ", Parameters: [serialization failed]";
                    }
                }
                
                // Add return value if configured
                if (performanceLog.logReturnValue()) {
                    try {
                        logMessage += ", Result: " + objectMapper.writeValueAsString(result);
                    } catch (Exception e) {
                        logMessage += ", Result: [serialization failed]";
                    }
                }
                
                if (executionTime > performanceLog.threshold()) {
                    log.warn(logMessage);
                } else {
                    log.info(logMessage);
                }
            }
            
            return result;
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String operation = !performanceLog.operation().isEmpty() ? performanceLog.operation() : methodName;
            
            log.error("PERFORMANCE_ERROR - Operation: {}, Class: {}, Method: {}, ExecutionTime: {}ms, Error: {}", 
                    operation, className, methodName, executionTime, throwable.getMessage());
            
            throw throwable;
        }
    }
}
