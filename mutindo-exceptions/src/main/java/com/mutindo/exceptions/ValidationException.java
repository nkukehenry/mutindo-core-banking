package com.mutindo.exceptions;

import java.util.List;

/**
 * Validation exceptions - focused only on input validation
 */
public class ValidationException extends CBSException {
    
    private final List<String> validationErrors;
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = List.of(message);
    }
    
    public ValidationException(List<String> validationErrors) {
        super("Validation failed", "VALIDATION_ERROR", 400);
        this.validationErrors = validationErrors;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
