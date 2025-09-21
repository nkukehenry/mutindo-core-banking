package com.mutindo.entities;

import com.mutindo.common.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity - system users
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_branch", columnList = "branchId"),
    @Index(name = "idx_user_type", columnList = "userType"),
    @Index(name = "idx_user_active", columnList = "active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @NotBlank
    @Size(max = 64)
    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    @NotBlank
    @Size(max = 128)
    @Column(name = "first_name", nullable = false, length = 128)
    private String firstName;

    @NotBlank
    @Size(max = 128)
    @Column(name = "last_name", nullable = false, length = 128)
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 128)
    @Column(name = "email", nullable = false, unique = true, length = 128)
    private String email;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 32)
    private UserType userType;

    @Column(name = "branch_id") // null for institution admins
    private Long branchId;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;

    @Size(max = 255)
    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    @Size(max = 64)
    @Column(name = "employee_id", length = 64)
    private String employeeId;

    @Size(max = 64)
    @Column(name = "department", length = 64)
    private String department;

    @Size(max = 64)
    @Column(name = "position", length = 64)
    private String position;

    @Column(name = "supervisor_id")
    private Long supervisorId;
}
