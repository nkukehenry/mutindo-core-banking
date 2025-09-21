package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User-Role association entity
 */
@Entity
@Table(name = "user_roles", indexes = {
    @Index(name = "idx_user_role_user", columnList = "userId"),
    @Index(name = "idx_user_role_role", columnList = "roleId"),
    @Index(name = "idx_user_role_unique", columnList = "userId, roleId", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @NotBlank
    @Size(max = 36)
    @Column(name = "role_id", nullable = false, length = 36)
    private String roleId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Size(max = 36)
    @Column(name = "assigned_by", length = 36)
    private String assignedBy;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
