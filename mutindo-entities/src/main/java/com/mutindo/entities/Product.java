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
import java.util.Map;

/**
 * Product entity - Account and Loan products
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_code", columnList = "code", unique = true),
    @Index(name = "idx_product_type", columnList = "productType"),
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_active", columnList = "active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @NotBlank
    @Size(max = 64)
    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Size(max = 32)
    @Column(name = "product_type", nullable = false, length = 32)
    private String productType; // SAVINGS, CURRENT, LOAN, FIXED_DEPOSIT, etc.

    @Size(max = 64)
    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // Interest rates
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "min_interest_rate", precision = 5, scale = 2)
    private BigDecimal minInterestRate;

    @Column(name = "max_interest_rate", precision = 5, scale = 2)
    private BigDecimal maxInterestRate;

    // Balance limits
    @Column(name = "min_balance", precision = 19, scale = 2)
    private BigDecimal minBalance;

    @Column(name = "max_balance", precision = 19, scale = 2)
    private BigDecimal maxBalance;

    @Column(name = "opening_balance", precision = 19, scale = 2)
    private BigDecimal openingBalance;

    // Transaction limits
    @Column(name = "daily_withdrawal_limit", precision = 19, scale = 2)
    private BigDecimal dailyWithdrawalLimit;

    @Column(name = "monthly_withdrawal_limit", precision = 19, scale = 2)
    private BigDecimal monthlyWithdrawalLimit;

    @Column(name = "max_transaction_amount", precision = 19, scale = 2)
    private BigDecimal maxTransactionAmount;

    // Fees
    @Column(name = "account_maintenance_fee", precision = 19, scale = 2)
    private BigDecimal accountMaintenanceFee;

    @Column(name = "transaction_fee", precision = 19, scale = 2)
    private BigDecimal transactionFee;

    // GL Account mappings
    @Size(max = 32)
    @Column(name = "liability_gl_code", length = 32) // For deposits
    private String liabilityGlCode;

    @Size(max = 32)
    @Column(name = "asset_gl_code", length = 32) // For loans
    private String assetGlCode;

    @Size(max = 32)
    @Column(name = "income_gl_code", length = 32) // For interest income
    private String incomeGlCode;

    @Size(max = 32)
    @Column(name = "expense_gl_code", length = 32) // For interest expense
    private String expenseGlCode;

    // Loan-specific fields
    @Column(name = "loan_term_min_months")
    private Integer loanTermMinMonths;

    @Column(name = "loan_term_max_months")
    private Integer loanTermMaxMonths;

    @Size(max = 32)
    @Column(name = "repayment_frequency", length = 32) // MONTHLY, WEEKLY, etc.
    private String repaymentFrequency;

    @Size(max = 32)
    @Column(name = "interest_calculation_method", length = 32) // FLAT, REDUCING_BALANCE
    private String interestCalculationMethod;

    // Eligibility criteria
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "eligibility_criteria", columnDefinition = "JSON")
    private Map<String, Object> eligibilityCriteria;

    // Product configuration
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration", columnDefinition = "JSON")
    private Map<String, Object> configuration;

    @Size(max = 8)
    @Column(name = "currency", length = 8)
    private String currency;

    @Column(name = "allows_overdraft", nullable = false)
    private Boolean allowsOverdraft = false;

    @Column(name = "requires_guarantor", nullable = false)
    private Boolean requiresGuarantor = false;

    @Column(name = "allows_partial_withdrawals", nullable = false)
    private Boolean allowsPartialWithdrawals = true;
}
