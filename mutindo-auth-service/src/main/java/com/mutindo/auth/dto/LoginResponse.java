package com.mutindo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Login response DTO - contains authentication result and user context
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private boolean success;
    private String message;
    
    // JWT tokens (reusing our existing JWT infrastructure)
    private String accessToken;
    private String refreshToken;
    private int expiresIn; // seconds
    private String tokenType; // "Bearer"
    
    // User information
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String userType;
    private String branchId; // null for institution admins
    private List<String> roles;
    private List<String> permissions;
    
    // Security info
    private LocalDateTime lastLoginAt;
    private boolean mustChangePassword;
    private boolean mfaEnabled;
    
    /**
     * Create successful login response
     */
    public static LoginResponse success(String accessToken, String refreshToken, int expiresIn, UserDto user) {
        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userType(user.getUserType())
                .branchId(user.getBranchId())
                .roles(user.getRoles())
                .permissions(user.getPermissions())
                .lastLoginAt(user.getLastLoginAt())
                .mustChangePassword(user.getMustChangePassword())
                .mfaEnabled(user.getMfaEnabled())
                .build();
    }
    
    /**
     * Create failed login response
     */
    public static LoginResponse failure(String message) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
