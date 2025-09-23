package com.mutindo.auth.dto;

import com.mutindo.common.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;
    
    private UserType userType;
    
    @Size(max = 36, message = "Branch ID must be at most 36 characters")
    private String branchId;
    
    private Boolean active;
    
    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;
}
