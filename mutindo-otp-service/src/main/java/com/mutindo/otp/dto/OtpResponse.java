package com.mutindo.otp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OTP response DTO - focused on operation result
 */
@Data
@Builder
public class OtpResponse {
    
    private boolean success;
    private String message;
    private String errorCode;
    private LocalDateTime expiresAt; // When OTP expires
    private Integer attemptsRemaining; // Remaining verification attempts
    private Integer cooldownSeconds; // Seconds before next OTP can be generated
    
    /**
     * Create successful OTP generation response
     */
    public static OtpResponse success(String message, LocalDateTime expiresAt) {
        return OtpResponse.builder()
                .success(true)
                .message(message)
                .expiresAt(expiresAt)
                .build();
    }
    
    /**
     * Create successful OTP verification response
     */
    public static OtpResponse verificationSuccess() {
        return OtpResponse.builder()
                .success(true)
                .message("OTP verified successfully")
                .build();
    }
    
    /**
     * Create failed response
     */
    public static OtpResponse failure(String message, String errorCode) {
        return OtpResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
    
    /**
     * Create failed response with attempts remaining
     */
    public static OtpResponse failure(String message, String errorCode, Integer attemptsRemaining) {
        return OtpResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .attemptsRemaining(attemptsRemaining)
                .build();
    }
    
    /**
     * Create cooldown response
     */
    public static OtpResponse cooldown(String message, Integer cooldownSeconds) {
        return OtpResponse.builder()
                .success(false)
                .message(message)
                .errorCode("OTP_COOLDOWN")
                .cooldownSeconds(cooldownSeconds)
                .build();
    }
}
