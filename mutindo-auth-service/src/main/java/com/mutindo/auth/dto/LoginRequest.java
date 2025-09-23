package com.mutindo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO - small and focused
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username or email is required")
    @Size(max = 128, message = "Username/email must be at most 128 characters")
    private String usernameOrEmail;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    private String password;
    
    private String deviceId; // Optional for device tracking
    private String ipAddress; // For security logging
}
