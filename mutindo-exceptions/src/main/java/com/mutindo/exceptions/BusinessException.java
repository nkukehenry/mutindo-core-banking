package com.mutindo.exceptions;

/**
 * Business logic exceptions - focused on business rule violations
 */
public class BusinessException extends CBSException {
    
    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", 400);
    }
    
    public BusinessException(String message, String errorCode) {
        super(message, errorCode, 400);
    }
}
