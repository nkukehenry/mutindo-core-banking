package com.mutindo.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * OTP verification request DTO - small and focused
 */
@Data
@Builder
public class OtpVerifyRequest {
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 32, message = "Phone number must be at most 32 characters")
    private String phoneNumber;
    
    @NotBlank(message = "OTP code is required")
    @Size(min = 4, max = 8, message = "OTP code must be between 4 and 8 characters")
    private String otpCode;
    
    @NotBlank(message = "Purpose is required")
    @Size(max = 64, message = "Purpose must be at most 64 characters")
    private String purpose; // Must match the generation purpose
    
    private String userId; // Optional - for user-specific OTP verification
}
