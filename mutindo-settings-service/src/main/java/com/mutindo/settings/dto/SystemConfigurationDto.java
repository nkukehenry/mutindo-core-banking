package com.mutindo.settings.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * System Configuration DTO for API responses
 */
@Data
@Builder
public class SystemConfigurationDto {
    
    private Long id;
    private String configKey;
    private String configName;
    private String configValue;
    private String description;
    private String dataType;
    private String category;
    private Boolean isEncrypted;
    private Boolean isSystem;
    private Boolean isEnvironmentSpecific;
    private Boolean active;
    private String defaultValue;
    private String validationRule;
    private String environment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
