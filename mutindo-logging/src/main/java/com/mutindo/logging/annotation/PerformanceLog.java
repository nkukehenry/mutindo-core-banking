package com.mutindo.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be performance logged
 * Provides configurable performance monitoring thresholds
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceLog {
    
    /**
     * Default value for backward compatibility
     */
    String value() default "";
    
    /**
     * Threshold in milliseconds - log if execution time exceeds this value
     * Default: 1000ms (1 second)
     */
    long threshold() default 1000L;
    
    /**
     * Whether to log method parameters (default: false for security)
     */
    boolean logParameters() default false;
    
    /**
     * Whether to log return values (default: false for security)
     */
    boolean logReturnValue() default false;
    
    /**
     * Custom operation name for logging
     */
    String operation() default "";
    
    /**
     * Whether to always log (ignore threshold)
     */
    boolean alwaysLog() default false;
}
