package com.mutindo.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audit logged
 * Provides rich metadata for comprehensive audit trails
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    
    /**
     * Default value for backward compatibility
     */
    String value() default "";
    
    /**
     * The action being performed (e.g., "CREATE_CUSTOMER", "UPDATE_ACCOUNT")
     */
    String action() default "";
    
    /**
     * The entity type being acted upon (e.g., "Customer", "Account", "Branch")
     */
    String entity() default "";
    
    /**
     * Additional description of the operation
     */
    String description() default "";
    
    /**
     * Whether to log sensitive data (default: false for security)
     */
    boolean includeSensitiveData() default false;
    
    /**
     * Priority level for audit logging
     */
    String priority() default "NORMAL";
}
