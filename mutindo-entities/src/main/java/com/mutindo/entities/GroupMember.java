package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Group Member entity - members of groups/SACCOs
 */
@Entity
@Table(name = "group_members", indexes = {
    @Index(name = "idx_group_member_group", columnList = "groupId"),
    @Index(name = "idx_group_member_customer", columnList = "customerId"),
    @Index(name = "idx_group_member_unique", columnList = "groupId, customerId", unique = true),
    @Index(name = "idx_group_member_status", columnList = "membershipStatus"),
    @Index(name = "idx_group_member_role", columnList = "memberRole")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "group_id", nullable = false, length = 36)
    private String groupId;

    @NotBlank
    @Size(max = 36)
    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @NotBlank
    @Size(max = 32)
    @Column(name = "membership_status", nullable = false, length = 32)
    private String membershipStatus; // ACTIVE, INACTIVE, SUSPENDED, EXPELLED, WITHDRAWN

    @Size(max = 32)
    @Column(name = "member_role", length = 32)
    private String memberRole; // LEADER, SECRETARY, TREASURER, MEMBER

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    @Size(max = 32)
    @Column(name = "member_number", length = 32)
    private String memberNumber; // Unique within the group

    @Column(name = "shares_owned")
    private Integer sharesOwned = 0;

    @Column(name = "total_contributions", precision = 19, scale = 2)
    private BigDecimal totalContributions = BigDecimal.ZERO;

    @Column(name = "total_loans", precision = 19, scale = 2)
    private BigDecimal totalLoans = BigDecimal.ZERO;

    @Column(name = "outstanding_loans", precision = 19, scale = 2)
    private BigDecimal outstandingLoans = BigDecimal.ZERO;

    @Column(name = "savings_balance", precision = 19, scale = 2)
    private BigDecimal savingsBalance = BigDecimal.ZERO;

    @Column(name = "guarantor_limit", precision = 19, scale = 2)
    private BigDecimal guarantorLimit = BigDecimal.ZERO;

    @Column(name = "current_guarantees", precision = 19, scale = 2)
    private BigDecimal currentGuarantees = BigDecimal.ZERO;

    @Column(name = "entry_fee_paid", precision = 19, scale = 2)
    private BigDecimal entryFeePaid = BigDecimal.ZERO;

    @Size(max = 36)
    @Column(name = "introduced_by", length = 36) // Customer ID who introduced this member
    private String introducedBy;

    @Size(max = 36)
    @Column(name = "approved_by", length = 36) // User ID who approved membership
    private String approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "exit_reason", columnDefinition = "TEXT")
    private String exitReason;

    @PrePersist
    protected void onCreate() {
        if (joinDate == null) {
            joinDate = LocalDate.now();
        }
    }
}
