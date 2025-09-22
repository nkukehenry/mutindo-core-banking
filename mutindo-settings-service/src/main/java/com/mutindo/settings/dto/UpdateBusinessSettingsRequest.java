package com.mutindo.settings.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Update Business Settings request DTO
 */
@Data
@Builder
public class UpdateBusinessSettingsRequest {
    
    @Size(max = 255, message = "Setting name must not exceed 255 characters")
    private String settingName;
    
    private String settingValue;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 32, message = "Data type must not exceed 32 characters")
    private String dataType;
    
    private Boolean isEncrypted;
    private Boolean isPublic;
    private String category;
    
    @Size(max = 255, message = "Validation rule must not exceed 255 characters")
    private String validationRule;
}
