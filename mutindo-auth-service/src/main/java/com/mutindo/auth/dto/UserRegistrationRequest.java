package com.mutindo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * User registration request DTO - for creating new users
 */
@Data
@Builder
public class UserRegistrationRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    private String username;
    
    @NotBlank(message = "First name is required")
    @Size(max = 128, message = "First name must be at most 128 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 128, message = "Last name must be at most 128 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 128, message = "Email must be at most 128 characters")
    private String email;
    
    @Size(max = 32, message = "Phone must be at most 32 characters")
    private String phone;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;
    
    @NotNull(message = "User type is required")
    private String userType; // INSTITUTION_ADMIN, BRANCH_USER
    
    @Size(max = 36, message = "Branch ID must be at most 36 characters")
    private String branchId; // Required for BRANCH_USER, null for INSTITUTION_ADMIN
    
    @Size(max = 64, message = "Employee ID must be at most 64 characters")
    private String employeeId;
    
    @Size(max = 64, message = "Department must be at most 64 characters")
    private String department;
    
    @Size(max = 64, message = "Position must be at most 64 characters")
    private String position;
    
    @Size(max = 36, message = "Supervisor ID must be at most 36 characters")
    private String supervisorId;
    
    // Roles to assign to the user
    private List<String> roleIds;
    
    // Force password change on first login
    private Boolean mustChangePassword = true;
}
