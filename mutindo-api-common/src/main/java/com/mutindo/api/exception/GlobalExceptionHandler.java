package com.mutindo.api.exception;

import com.mutindo.common.dto.BaseResponse; // Reusing existing response wrapper
import com.mutindo.exceptions.BusinessException; // Reusing existing exceptions
import com.mutindo.exceptions.ValidationException; // Reusing existing exceptions
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for consistent API error responses
 * Reuses existing BaseResponse and exception infrastructure
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle business exceptions with appropriate HTTP status
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        log.warn("Business exception: {} - Error code: {}", ex.getMessage(), ex.getErrorCode());
        
        BaseResponse<Void> response = BaseResponse.<Void>error(ex.getMessage())
                .toBuilder()
                .correlationId(getCorrelationId(request))
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        log.warn("Validation exception: {}", ex.getMessage());
        
        BaseResponse<Void> response = BaseResponse.<Void>error(ex.getMessage(), ex.getValidationErrors())
                .toBuilder()
                .correlationId(getCorrelationId(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle Spring validation errors (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Method argument validation failed: {}", ex.getMessage());
        
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        
        BaseResponse<Void> response = BaseResponse.<Void>error("Validation failed", errors)
                .toBuilder()
                .correlationId(getCorrelationId(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected exception", ex);
        
        BaseResponse<Void> response = BaseResponse.<Void>error("An unexpected error occurred")
                .toBuilder()
                .correlationId(getCorrelationId(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Illegal argument exception: {}", ex.getMessage());
        
        BaseResponse<Void> response = BaseResponse.<Void>error(ex.getMessage())
                .toBuilder()
                .correlationId(getCorrelationId(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Private helper methods (small and focused)

    /**
     * Format field error for user-friendly message
     */
    private String formatFieldError(FieldError fieldError) {
        return String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage());
    }

    /**
     * Extract correlation ID from request
     */
    private String getCorrelationId(WebRequest request) {
        return request.getHeader("X-Correlation-ID");
    }
}
