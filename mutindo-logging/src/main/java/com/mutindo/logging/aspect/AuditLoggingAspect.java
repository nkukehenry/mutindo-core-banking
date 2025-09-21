package com.mutindo.logging.aspect;

import com.mutindo.common.context.BranchContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Aspect for audit logging of sensitive operations
 */
@Aspect
@Component
@Slf4j
public class AuditLoggingAspect {

    @Autowired
    private ObjectMapper objectMapper;

    @Before("@annotation(com.annotation.logging.mutindo.AuditLog)")
    public void logBefore(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String userId = BranchContextHolder.getCurrentUserId();
            String branchId = BranchContextHolder.getCurrentBranchId();

            log.info("AUDIT_START - User: {}, Branch: {}, Class: {}, Method: {}, Args: {}",
                    userId, branchId, className, methodName,
                    objectMapper.writeValueAsString(joinPoint.getArgs()));

        } catch (Exception e) {
            log.warn("Failed to log audit information", e);
        }
    }

    @AfterReturning(pointcut = "@annotation(com.annotation.logging.mutindo.AuditLog)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String userId = BranchContextHolder.getCurrentUserId();
            String branchId = BranchContextHolder.getCurrentBranchId();

            log.info("AUDIT_SUCCESS - User: {}, Branch: {}, Class: {}, Method: {}, Result: {}",
                    userId, branchId, className, methodName,
                    objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            log.warn("Failed to log audit information", e);
        }
    }

    @AfterThrowing(pointcut = "@annotation(com.annotation.logging.mutindo.AuditLog)", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String userId = BranchContextHolder.getCurrentUserId();
            String branchId = BranchContextHolder.getCurrentBranchId();

            log.error("AUDIT_ERROR - User: {}, Branch: {}, Class: {}, Method: {}, Error: {}",
                    userId, branchId, className, methodName, exception.getMessage(), exception);

        } catch (Exception e) {
            log.warn("Failed to log audit information", e);
        }
    }
}
