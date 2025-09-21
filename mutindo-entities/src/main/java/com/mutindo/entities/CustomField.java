package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * Custom Field entity - dynamic field definitions
 */
@Entity
@Table(name = "custom_fields", indexes = {
    @Index(name = "idx_custom_field_entity", columnList = "entityType"),
    @Index(name = "idx_custom_field_code", columnList = "code"),
    @Index(name = "idx_custom_field_branch", columnList = "branchScope"),
    @Index(name = "idx_custom_field_indexed", columnList = "isIndexed")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomField extends BaseEntity {

    @NotBlank
    @Size(max = 32)
    @Column(name = "entity_type", nullable = false, length = 32)
    private String entityType; // CUSTOMER, ACCOUNT, LOAN, etc.

    @NotBlank
    @Size(max = 64)
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @NotBlank
    @Size(max = 128)
    @Column(name = "label", nullable = false, length = 128)
    private String label;

    @NotBlank
    @Size(max = 32)
    @Column(name = "data_type", nullable = false, length = 32)
    private String dataType; // TEXT, NUMBER, DATE, SELECT, FILE, BIOMETRIC_REF

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation", columnDefinition = "JSON")
    private Map<String, Object> validation; // regex, min, max, required, etc.

    @Size(max = 32)
    @Column(name = "visibility", length = 32)
    private String visibility; // PUBLIC, INTERNAL, RESTRICTED

    @Size(max = 36)
    @Column(name = "branch_scope", length = 36) // null for global fields
    private String branchScope;

    @Size(max = 64)
    @Column(name = "ui_hint", length = 64)
    private String uiHint; // TEXTBOX, DROPDOWN, CHECKBOX, etc.

    @Column(name = "is_indexed", nullable = false)
    private Boolean isIndexed = false; // For searchable fields

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "required", nullable = false)
    private Boolean required = false;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "JSON")
    private Map<String, Object> options; // For SELECT type fields
}
