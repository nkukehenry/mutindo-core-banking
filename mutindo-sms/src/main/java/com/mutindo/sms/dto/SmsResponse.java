package com.mutindo.sms.dto;

import lombok.Builder;
import lombok.Data;

/**
 * SMS response DTO - minimal and focused
 */
@Data
@Builder
public class SmsResponse {
    private boolean success;
    private String message;
    private String messageId; // Gateway message ID for tracking
    
    public static SmsResponse success(String message) {
        return SmsResponse.builder()
                .success(true)
                .message(message)
                .build();
    }
    
    public static SmsResponse failed(String message) {
        return SmsResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
