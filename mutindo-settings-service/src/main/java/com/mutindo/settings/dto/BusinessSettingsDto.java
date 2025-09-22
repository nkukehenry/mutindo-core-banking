package com.mutindo.settings.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Business Settings DTO for API responses
 */
@Data
@Builder
public class BusinessSettingsDto {
    
    private Long id;
    private String settingKey;
    private String settingType;
    private String settingName;
    private String settingValue;
    private String description;
    private String dataType;
    private Boolean isEncrypted;
    private Boolean isSystem;
    private Boolean isPublic;
    private Boolean active;
    private String category;
    private String validationRule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
