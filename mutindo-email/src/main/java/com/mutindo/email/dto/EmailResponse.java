package com.mutindo.email.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Email response DTO - minimal and focused
 */
@Data
@Builder
public class EmailResponse {
    private boolean success;
    private String message;
    private String messageId; // Email message ID for tracking
    
    public static EmailResponse success(String message) {
        return EmailResponse.builder()
                .success(true)
                .message(message)
                .build();
    }
    
    public static EmailResponse failed(String message) {
        return EmailResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
