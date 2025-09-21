package com.mutindo.chartofaccounts.dto;

import com.mutindo.common.enums.GLAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for creating GL accounts
 */
@Data
@Builder
public class CreateGLAccountRequest {
    
    @NotBlank(message = "Account code is required")
    @Size(max = 32, message = "Account code must be at most 32 characters")
    private String code;
    
    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must be at most 255 characters")
    private String name;
    
    @NotNull(message = "Account type is required")
    private GLAccountType type;
    
    @Size(max = 36, message = "Parent ID must be at most 36 characters")
    private Long parentId;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 8, message = "Currency must be at most 8 characters")
    private String currency;
    
    private Boolean isControlAccount = false;
    
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
    
    @Size(max = 32, message = "Category must be at most 32 characters")
    private String category;
    
    private Boolean allowsPosting = true;
}
