package com.mutindo.logging.aspect;

import com.mutindo.common.context.BranchContextHolder;
import com.mutindo.logging.annotation.AuditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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

    @Before("@annotation(auditLog)")
    public void logBefore(JoinPoint joinPoint, AuditLog auditLog) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            Long userId = BranchContextHolder.getCurrentUserId();
            Long branchId = BranchContextHolder.getCurrentBranchId();
            
            // Get enhanced annotation metadata
            String action = !auditLog.action().isEmpty() ? auditLog.action() : auditLog.value();
            String entity = auditLog.entity();
            String description = auditLog.description();
            
            String logMessage = String.format("AUDIT_START - Action: %s, Entity: %s, User: %s, Branch: %s, Class: %s, Method: %s", 
                    action, entity, userId, branchId, className, methodName);
            
            if (!description.isEmpty()) {
                logMessage += ", Description: " + description;
            }
            
            // Log parameters only if not sensitive data
            if (auditLog.includeSensitiveData()) {
                logMessage += ", Args: " + objectMapper.writeValueAsString(joinPoint.getArgs());
            }
            
            log.info(logMessage);

        } catch (Exception e) {
            log.warn("Failed to log audit information", e);
        }
    }

    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            Long userId = BranchContextHolder.getCurrentUserId();
            Long branchId = BranchContextHolder.getCurrentBranchId();
            
            // Get enhanced annotation metadata
            String action = !auditLog.action().isEmpty() ? auditLog.action() : auditLog.value();
            String entity = auditLog.entity();
            
            String logMessage = String.format("AUDIT_SUCCESS - Action: %s, Entity: %s, User: %s, Branch: %s, Class: %s, Method: %s", 
                    action, entity, userId, branchId, className, methodName);
            
            // Log result only if not sensitive data
            if (auditLog.includeSensitiveData()) {
                logMessage += ", Result: " + objectMapper.writeValueAsString(result);
            }
            
            log.info(logMessage);

        } catch (Exception e) {
            log.warn("Failed to log audit information", e);
        }
    }

    @AfterThrowing(pointcut = "@annotation(auditLog)", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, AuditLog auditLog, Throwable exception) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            Long userId = BranchContextHolder.getCurrentUserId();
            Long branchId = BranchContextHolder.getCurrentBranchId();
            
            // Get enhanced annotation metadata
            String action = !auditLog.action().isEmpty() ? auditLog.action() : auditLog.value();
            String entity = auditLog.entity();
            
            log.error("AUDIT_ERROR - Action: {}, Entity: {}, User: {}, Branch: {}, Class: {}, Method: {}, Error: {}",
                    action, entity, userId, branchId, className, methodName, exception.getMessage(), exception);

        } catch (Exception e) {
            log.warn("Failed to log audit information", e);
        }
    }
}
