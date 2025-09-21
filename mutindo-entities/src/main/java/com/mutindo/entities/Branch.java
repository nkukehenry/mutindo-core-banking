package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Branch entity - focused only on data model
 */
@Entity
@Table(name = "branches", indexes = {
    @Index(name = "idx_branch_code", columnList = "code", unique = true),
    @Index(name = "idx_branch_active", columnList = "active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Branch extends BaseEntity {

    @NotBlank
    @Size(max = 16)
    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 64)
    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @Size(max = 128)
    @Column(name = "email", length = 128)
    private String email;

    @Size(max = 64)
    @Column(name = "manager_id", length = 64)
    private String managerId;
}
