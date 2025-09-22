package com.mutindo.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Create Business Settings request DTO
 */
@Data
@Builder
public class CreateBusinessSettingsRequest {
    
    @NotBlank(message = "Setting key is required")
    @Size(min = 3, max = 64, message = "Setting key must be between 3 and 64 characters")
    private String settingKey;
    
    @NotBlank(message = "Setting type is required")
    @Size(max = 32, message = "Setting type must not exceed 32 characters")
    private String settingType;
    
    @Size(max = 255, message = "Setting name must not exceed 255 characters")
    private String settingName;
    
    private String settingValue;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 32, message = "Data type must not exceed 32 characters")
    private String dataType;
    
    private Boolean isEncrypted;
    private Boolean isSystem;
    private Boolean isPublic;
    private String category;
    
    @Size(max = 255, message = "Validation rule must not exceed 255 characters")
    private String validationRule;
}
