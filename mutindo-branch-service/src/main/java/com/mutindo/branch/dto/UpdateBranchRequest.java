package com.mutindo.branch.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Update branch request DTO
 */
@Data
@Builder
public class UpdateBranchRequest {
    
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
