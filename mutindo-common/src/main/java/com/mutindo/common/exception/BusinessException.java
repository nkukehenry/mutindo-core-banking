package com.mutindo.common.exception;

/**
 * Business logic exceptions
 */
public class BusinessException extends CBSException {
    
    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", 400);
    }
    
    public BusinessException(String message, String errorCode) {
        super(message, errorCode, 400);
    }
}
