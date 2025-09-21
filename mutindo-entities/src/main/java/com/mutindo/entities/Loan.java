package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 * Loan entity
 */
@Entity
@Table(name = "loans", indexes = {
    @Index(name = "idx_loan_account", columnList = "loanAccountId"),
    @Index(name = "idx_loan_customer", columnList = "customerId"),
    @Index(name = "idx_loan_product", columnList = "productCode"),
    @Index(name = "idx_loan_status", columnList = "status"),
    @Index(name = "idx_loan_branch", columnList = "branchId")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Loan extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "loan_account_id", nullable = false, length = 36)
    private String loanAccountId; // Points to accounts table

    @NotBlank
    @Size(max = 36)
    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "product_code", nullable = false, length = 64)
    private String productCode;

    @NotBlank
    @Size(max = 36)
    @Column(name = "branch_id", nullable = false, length = 36)
    private String branchId;

    @NotNull
    @Column(name = "principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    @Column(name = "disbursed_amount", precision = 19, scale = 2)
    private BigDecimal disbursedAmount;

    @NotNull
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @NotBlank
    @Size(max = 32)
    @Column(name = "interest_method", nullable = false, length = 32)
    private String interestMethod; // FLAT, REDUCING_BALANCE, etc.

    @NotNull
    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @NotBlank
    @Size(max = 32)
    @Column(name = "repayment_frequency", nullable = false, length = 32)
    private String repaymentFrequency; // MONTHLY, WEEKLY, etc.

    @NotBlank
    @Size(max = 32)
    @Column(name = "status", nullable = false, length = 32)
    private String status; // PENDING, APPROVED, DISBURSED, ACTIVE, CLOSED, etc.

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Size(max = 36)
    @Column(name = "approved_by", length = 36)
    private String approvedBy;

    @Size(max = 36)
    @Column(name = "disbursed_by", length = 36)
    private String disbursedBy;

    @Column(name = "outstanding_principal", precision = 19, scale = 2)
    private BigDecimal outstandingPrincipal;

    @Column(name = "outstanding_interest", precision = 19, scale = 2)
    private BigDecimal outstandingInterest;

    @Column(name = "outstanding_fees", precision = 19, scale = 2)
    private BigDecimal outstandingFees;

    @Column(name = "total_paid", precision = 19, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "days_in_arrears")
    private Integer daysInArrears = 0;

    @Size(max = 36)
    @Column(name = "group_id", length = 36) // For group loans
    private String groupId;

    @Size(max = 36)
    @Column(name = "guarantor_id", length = 36)
    private String guarantorId;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    // Custom fields stored as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_data", columnDefinition = "JSON")
    private Map<String, Object> customData;
}
