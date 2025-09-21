package com.mutindo.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * OTP generation request DTO - small and focused
 */
@Data
@Builder
public class OtpGenerateRequest {
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 32, message = "Phone number must be at most 32 characters")
    private String phoneNumber;
    
    @NotBlank(message = "Purpose is required")
    @Size(max = 64, message = "Purpose must be at most 64 characters")
    private String purpose; // LOGIN, TRANSACTION, RESET_PASSWORD, etc.
    
    private String userId; // Optional - for user-specific OTP
    private Integer validityMinutes; // Optional - custom validity period
}
