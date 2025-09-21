package com.mutindo.sms.dto;

import lombok.Builder;
import lombok.Data;

/**
 * SMS request DTO - minimal and focused
 */
@Data
@Builder
public class SmsRequest {
    private String phoneNumber;
    private String message;
    private String sender; // Optional sender ID
}
