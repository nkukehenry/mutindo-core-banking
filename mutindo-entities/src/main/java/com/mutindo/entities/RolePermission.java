package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Role-Permission association entity
 */
@Entity
@Table(name = "role_permissions", indexes = {
    @Index(name = "idx_role_perm_role", columnList = "roleId"),
    @Index(name = "idx_role_perm_permission", columnList = "permissionId"),
    @Index(name = "idx_role_perm_unique", columnList = "roleId, permissionId", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "role_id", nullable = false, length = 36)
    private String roleId;

    @NotBlank
    @Size(max = 36)
    @Column(name = "permission_id", nullable = false, length = 36)
    private String permissionId;
}
