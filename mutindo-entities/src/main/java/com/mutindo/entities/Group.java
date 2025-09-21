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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Group entity - for group banking, SACCOs, and joint liability groups
 */
@Entity
@Table(name = "groups", indexes = {
    @Index(name = "idx_group_code", columnList = "groupCode", unique = true),
    @Index(name = "idx_group_branch", columnList = "branchId"),
    @Index(name = "idx_group_type", columnList = "groupType"),
    @Index(name = "idx_group_status", columnList = "status"),
    @Index(name = "idx_group_leader", columnList = "groupLeaderId")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Group extends BaseEntity {

    @NotBlank
    @Size(max = 32)
    @Column(name = "group_code", nullable = false, unique = true, length = 32)
    private String groupCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "group_name", nullable = false)
    private String groupName;

    @NotBlank
    @Size(max = 32)
    @Column(name = "group_type", nullable = false, length = 32)
    private String groupType; // SACCO, CHAMA, COOPERATIVE, JOINT_LIABILITY, etc.

    @NotBlank
    @Size(max = 36)
    @Column(name = "branch_id", nullable = false, length = 36)
    private String branchId;

    @Size(max = 36)
    @Column(name = "group_leader_id", length = 36) // Customer ID of group leader
    private String groupLeaderId;

    @Size(max = 36)
    @Column(name = "secretary_id", length = 36) // Customer ID of secretary
    private String secretaryId;

    @Size(max = 36)
    @Column(name = "treasurer_id", length = 36) // Customer ID of treasurer
    private String treasurerId;

    @NotBlank
    @Size(max = 32)
    @Column(name = "status", nullable = false, length = 32)
    private String status; // ACTIVE, INACTIVE, SUSPENDED, DISSOLVED

    @Column(name = "formation_date")
    private LocalDate formationDate;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Size(max = 128)
    @Column(name = "registration_number", length = 128)
    private String registrationNumber;

    @Column(name = "meeting_frequency", length = 32)
    private String meetingFrequency; // WEEKLY, MONTHLY, QUARTERLY

    @Column(name = "meeting_day", length = 16)
    private String meetingDay; // MONDAY, TUESDAY, etc.

    @Column(name = "meeting_venue", columnDefinition = "TEXT")
    private String meetingVenue;

    @Column(name = "min_members")
    private Integer minMembers;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "current_members")
    private Integer currentMembers = 0;

    @Column(name = "entry_fee", precision = 19, scale = 2)
    private BigDecimal entryFee;

    @Column(name = "monthly_contribution", precision = 19, scale = 2)
    private BigDecimal monthlyContribution;

    @Column(name = "share_value", precision = 19, scale = 2)
    private BigDecimal shareValue;

    @Column(name = "total_shares")
    private Integer totalShares = 0;

    @Column(name = "total_savings", precision = 19, scale = 2)
    private BigDecimal totalSavings = BigDecimal.ZERO;

    @Column(name = "total_loans", precision = 19, scale = 2)
    private BigDecimal totalLoans = BigDecimal.ZERO;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @Size(max = 128)
    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "constitution", columnDefinition = "TEXT")
    private String constitution; // Group rules and constitution

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // Custom fields stored as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_data", columnDefinition = "JSON")
    private Map<String, Object> customData;

    @PrePersist
    protected void onCreate() {
        if (formationDate == null) {
            formationDate = LocalDate.now();
        }
    }
}
