package com.mutindo.settings.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Update System Configuration request DTO
 */
@Data
@Builder
public class UpdateSystemConfigurationRequest {
    
    @Size(max = 255, message = "Config name must not exceed 255 characters")
    private String configName;
    
    private String configValue;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 32, message = "Data type must not exceed 32 characters")
    private String dataType;
    
    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;
    
    private Boolean isEncrypted;
    private Boolean isEnvironmentSpecific;
    private String defaultValue;
    
    @Size(max = 1000, message = "Validation rule must not exceed 1000 characters")
    private String validationRule;
    
    @Size(max = 255, message = "Environment must not exceed 255 characters")
    private String environment;
}
