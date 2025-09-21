package com.mutindo.common.exception;

import lombok.Getter;

/**
 * Base exception for CBS application
 */
@Getter
public class CBSException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    
    public CBSException(String message) {
        super(message);
        this.errorCode = "CBS_ERROR";
        this.httpStatus = 500;
    }
    
    public CBSException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }
    
    public CBSException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public CBSException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CBS_ERROR";
        this.httpStatus = 500;
    }
    
    public CBSException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }
}
