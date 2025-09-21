package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Permission entity - system permissions
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "name", unique = true),
    @Index(name = "idx_permission_resource", columnList = "resource"),
    @Index(name = "idx_permission_action", columnList = "action")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {

    @NotBlank
    @Size(max = 64)
    @Column(name = "name", nullable = false, unique = true, length = 64)
    private String name; // e.g., "accounts:create", "loans:approve"

    @NotBlank
    @Size(max = 32)
    @Column(name = "resource", nullable = false, length = 32)
    private String resource; // e.g., "accounts", "loans", "users"

    @NotBlank
    @Size(max = 32)
    @Column(name = "action", nullable = false, length = 32)
    private String action; // e.g., "create", "read", "update", "delete", "approve"

    @Size(max = 255)
    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
