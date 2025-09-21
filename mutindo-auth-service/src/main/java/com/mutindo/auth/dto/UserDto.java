package com.mutindo.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User DTO for API responses - focused on essential user information
 */
@Data
@Builder
public class UserDto {
    
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String userType; // INSTITUTION_ADMIN, BRANCH_USER
    private String branchId; // null for institution admins
    private Boolean active;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean mfaEnabled;
    private LocalDateTime lastLoginAt;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private Boolean mustChangePassword;
    private String employeeId;
    private String department;
    private String position;
    private String supervisorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Roles and permissions (computed fields)
    private List<String> roles;
    private List<String> permissions;
}
