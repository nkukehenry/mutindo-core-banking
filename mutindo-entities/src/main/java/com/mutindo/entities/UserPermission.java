package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * User-Permission override entity - for user-level permission overrides
 */
@Entity
@Table(name = "user_permissions", indexes = {
    @Index(name = "idx_user_perm_user", columnList = "userId"),
    @Index(name = "idx_user_perm_permission", columnList = "permissionId"),
    @Index(name = "idx_user_perm_unique", columnList = "userId, permissionId", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @NotBlank
    @Size(max = 36)
    @Column(name = "permission_id", nullable = false, length = 36)
    private String permissionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false, length = 16)
    private PermissionEffect effect; // GRANT or DENY

    public enum PermissionEffect {
        GRANT, DENY
    }
}
