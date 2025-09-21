package com.mutindo.entities;

import com.mutindo.common.enums.GLAccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * General Ledger Account entity - chart of accounts
 */
@Entity
@Table(name = "gl_accounts", indexes = {
    @Index(name = "idx_gl_code", columnList = "code", unique = true),
    @Index(name = "idx_gl_type", columnList = "type"),
    @Index(name = "idx_gl_parent", columnList = "parentId"),
    @Index(name = "idx_gl_control", columnList = "isControlAccount")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GLAccount extends BaseEntity {

    @NotBlank
    @Size(max = 32)
    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private GLAccountType type;

    @Column(name = "parent_id")
    private Long parentId;

    @NotBlank
    @Size(max = 8)
    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "is_control_account", nullable = false)
    private Boolean isControlAccount = false;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 32)
    @Column(name = "category", length = 32)
    private String category;

    @Column(name = "level")
    private Integer level;

    @Column(name = "allows_posting", nullable = false)
    private Boolean allowsPosting = true; // Leaf accounts allow posting, parent accounts don't
}
