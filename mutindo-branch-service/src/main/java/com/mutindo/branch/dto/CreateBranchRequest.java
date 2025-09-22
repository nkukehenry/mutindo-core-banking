package com.mutindo.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Create branch request DTO
 */
@Data
@Builder
public class CreateBranchRequest {
    
    @NotBlank(message = "Branch code is required")
    @Size(min = 3, max = 16, message = "Branch code must be between 3 and 16 characters")
    private String code;
    
    @NotBlank(message = "Branch name is required")
    @Size(min = 3, max = 255, message = "Branch name must be between 3 and 255 characters")
    private String name;
    
    @Size(max = 64, message = "Timezone must not exceed 64 characters")
    private String timezone;
    
    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    private String address;
    
    @Size(max = 32, message = "Phone must not exceed 32 characters")
    private String phone;
    
    @Size(max = 128, message = "Email must not exceed 128 characters")
    private String email;
    
    @Size(max = 64, message = "Manager ID must not exceed 64 characters")
    private String managerId;
}
