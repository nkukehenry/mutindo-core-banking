package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Group Loan entity - loans taken by groups or with group guarantee
 */
@Entity
@Table(name = "group_loans", indexes = {
    @Index(name = "idx_group_loan_group", columnList = "groupId"),
    @Index(name = "idx_group_loan_loan", columnList = "loanId"),
    @Index(name = "idx_group_loan_borrower", columnList = "borrowerCustomerId"),
    @Index(name = "idx_group_loan_type", columnList = "loanType"),
    @Index(name = "idx_group_loan_unique", columnList = "groupId, loanId", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GroupLoan extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "group_id", nullable = false, length = 36)
    private String groupId;

    @NotBlank
    @Size(max = 36)
    @Column(name = "loan_id", nullable = false, length = 36)
    private String loanId;

    @Size(max = 36)
    @Column(name = "borrower_customer_id", length = 36) // null for group loans, specific customer for individual loans with group guarantee
    private String borrowerCustomerId;

    @NotBlank
    @Size(max = 32)
    @Column(name = "loan_type", nullable = false, length = 32)
    private String loanType; // GROUP_LOAN, INDIVIDUAL_WITH_GROUP_GUARANTEE

    @NotNull
    @Column(name = "group_liability_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal groupLiabilityPercentage; // Percentage of loan guaranteed by group

    @Column(name = "individual_liability_percentage", precision = 5, scale = 2)
    private BigDecimal individualLiabilityPercentage; // Percentage of loan guaranteed by individual

    @Column(name = "group_guarantee_amount", precision = 19, scale = 2)
    private BigDecimal groupGuaranteeAmount;

    @Column(name = "meeting_approval_required", nullable = false)
    private Boolean meetingApprovalRequired = true;

    @Column(name = "meeting_approved", nullable = false)
    private Boolean meetingApproved = false;

    @Size(max = 64)
    @Column(name = "meeting_minutes_ref", length = 64)
    private String meetingMinutesRef;

    @Column(name = "collateral_required", nullable = false)
    private Boolean collateralRequired = false;

    @Column(name = "collateral_value", precision = 19, scale = 2)
    private BigDecimal collateralValue;

    @Column(name = "collateral_description", columnDefinition = "TEXT")
    private String collateralDescription;
}
