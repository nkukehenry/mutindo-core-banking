package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * System Configuration entity - stores system-wide configuration
 */
@Entity
@Table(name = "system_configurations", indexes = {
    @Index(name = "idx_system_config_key", columnList = "configKey", unique = true),
    @Index(name = "idx_system_config_category", columnList = "category"),
    @Index(name = "idx_system_config_active", columnList = "active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration extends BaseEntity {

    @NotBlank
    @Size(max = 64)
    @Column(name = "config_key", nullable = false, unique = true, length = 64)
    private String configKey;

    @NotBlank
    @Size(max = 255)
    @Column(name = "config_name", nullable = false, length = 255)
    private String configName;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 32)
    @Column(name = "data_type", nullable = false, length = 32)
    private String dataType; // STRING, NUMBER, BOOLEAN, JSON, FILE

    @Size(max = 64)
    @Column(name = "category", nullable = false, length = 64)
    private String category; // EMAIL, SMS, SECURITY, INTEGRATION, etc.

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false; // System configs cannot be deleted

    @Column(name = "is_environment_specific", nullable = false)
    private Boolean isEnvironmentSpecific = false; // Different per environment

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 255)
    @Column(name = "default_value", length = 255)
    private String defaultValue;

    @Size(max = 1000)
    @Column(name = "validation_rule", length = 1000)
    private String validationRule; // JSON validation rules

    @Size(max = 255)
    @Column(name = "environment", length = 255)
    private String environment; // DEV, TEST, PROD, etc.
}
