package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Custom Field Value entity - stores actual values for custom fields
 * Polymorphic design to attach to any entity type
 */
@Entity
@Table(name = "custom_field_values", indexes = {
    @Index(name = "idx_cfv_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_cfv_field", columnList = "customFieldId"),
    @Index(name = "idx_cfv_branch", columnList = "branchId"),
    @Index(name = "idx_cfv_searchable", columnList = "isSearchable, textValue"),
    @Index(name = "idx_cfv_numeric", columnList = "numericValue"),
    @Index(name = "idx_cfv_date", columnList = "dateValue")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomFieldValue extends BaseEntity {

    @NotNull
    @Column(name = "custom_field_id", nullable = false)
    private Long customFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_field_id", insertable = false, updatable = false)
    private CustomField customField;

    @NotBlank
    @Size(max = 32)
    @Column(name = "entity_type", nullable = false, length = 32)
    private String entityType; // CUSTOMER, ACCOUNT, LOAN, etc.

    @NotNull
    @Column(name = "entity_id", nullable = false)
    private Long entityId; // ID of the entity this field belongs to

    @Column(name = "branch_id")
    private Long branchId; // For branch-scoped fields

    // Multiple value storage columns for different data types and indexing
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_value", columnDefinition = "JSON")
    private Object jsonValue; // Primary storage for complex values

    @Size(max = 1000)
    @Column(name = "text_value", length = 1000)
    private String textValue; // For searchable text values

    @Column(name = "numeric_value", precision = 19, scale = 4)
    private java.math.BigDecimal numericValue; // For numeric values and sorting

    @Column(name = "date_value")
    private java.time.LocalDate dateValue; // For date values and filtering

    @Column(name = "datetime_value")
    private java.time.LocalDateTime datetimeValue; // For datetime values

    @Column(name = "boolean_value")
    private Boolean booleanValue; // For boolean values

    @Size(max = 500)
    @Column(name = "file_path", length = 500)
    private String filePath; // For file uploads

    @Column(name = "is_searchable", nullable = false)
    private Boolean isSearchable = false; // Copy from CustomField for performance

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false; // For sensitive data

    // Validation status
    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true;

    @Size(max = 500)
    @Column(name = "validation_error", length = 500)
    private String validationError;

    // Versioning for audit trail
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Size(max = 500)
    @Column(name = "change_reason", length = 500)
    private String changeReason;

    /**
     * Get the actual value based on the field's data type
     */
    @Transient
    public Object getValue() {
        if (customField == null) {
            return jsonValue; // Fallback to JSON value
        }
        
        return switch (customField.getDataType().toUpperCase()) {
            case "TEXT", "EMAIL", "PHONE", "SELECT" -> textValue;
            case "NUMBER", "CURRENCY", "PERCENTAGE" -> numericValue;
            case "DATE" -> dateValue;
            case "DATETIME", "TIMESTAMP" -> datetimeValue;
            case "BOOLEAN", "CHECKBOX" -> booleanValue;
            case "FILE", "IMAGE", "DOCUMENT" -> filePath;
            case "JSON", "OBJECT", "ARRAY" -> jsonValue;
            default -> textValue; // Default to text
        };
    }

    /**
     * Set the value based on the field's data type
     */
    @Transient
    public void setValue(Object value) {
        if (value == null) {
            clearAllValues();
            return;
        }

        // Always store in JSON for complex queries
        this.jsonValue = value;

        // Also store in appropriate typed column for indexing/searching
        if (value instanceof String) {
            this.textValue = (String) value;
        } else if (value instanceof Number) {
            this.numericValue = new java.math.BigDecimal(value.toString());
        } else if (value instanceof java.time.LocalDate) {
            this.dateValue = (java.time.LocalDate) value;
        } else if (value instanceof java.time.LocalDateTime) {
            this.datetimeValue = (java.time.LocalDateTime) value;
        } else if (value instanceof Boolean) {
            this.booleanValue = (Boolean) value;
        } else {
            // Convert to string for searching
            this.textValue = value.toString();
        }
    }

    private void clearAllValues() {
        this.jsonValue = null;
        this.textValue = null;
        this.numericValue = null;
        this.dateValue = null;
        this.datetimeValue = null;
        this.booleanValue = null;
        this.filePath = null;
    }
}
