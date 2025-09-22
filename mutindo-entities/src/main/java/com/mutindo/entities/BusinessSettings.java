package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Business Settings entity - stores bank/business configuration
 */
@Entity
@Table(name = "business_settings", indexes = {
    @Index(name = "idx_business_settings_active", columnList = "active"),
    @Index(name = "idx_business_settings_type", columnList = "settingType")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSettings extends BaseEntity {

    @NotBlank
    @Size(max = 64)
    @Column(name = "setting_key", nullable = false, unique = true, length = 64)
    private String settingKey;

    @NotBlank
    @Size(max = 32)
    @Column(name = "setting_type", nullable = false, length = 32)
    private String settingType; // BUSINESS_INFO, SYSTEM_CONFIG, LOCATION, BRANDING, etc.

    @Size(max = 255)
    @Column(name = "setting_name", length = 255)
    private String settingName;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 32)
    @Column(name = "data_type", length = 32)
    private String dataType; // STRING, NUMBER, BOOLEAN, JSON, FILE

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false; // System settings cannot be deleted

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false; // Public settings visible to all users

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 64)
    @Column(name = "category", length = 64)
    private String category; // For grouping settings

    @Size(max = 255)
    @Column(name = "validation_rule", length = 255)
    private String validationRule; // JSON validation rules
}
