package com.mutindo.entities;

import com.mutindo.common.enums.AccountStatus;
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
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Account entity - focused only on data model
 */
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_number", columnList = "accountNumber", unique = true),
    @Index(name = "idx_account_customer", columnList = "customerId"),
    @Index(name = "idx_account_branch", columnList = "branchId"),
    @Index(name = "idx_account_product", columnList = "productCode"),
    @Index(name = "idx_account_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @NotBlank
    @Size(max = 32)
    @Column(name = "account_number", nullable = false, unique = true, length = 32)
    private String accountNumber;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotNull
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @NotBlank
    @Size(max = 8)
    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @NotNull
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull
    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Size(max = 64)
    @Column(name = "closed_by", length = 64)
    private String closedBy;

    @Column(name = "closure_reason", columnDefinition = "TEXT")
    private String closureReason;

    // Account limits and settings
    @Column(name = "daily_withdrawal_limit", precision = 19, scale = 2)
    private BigDecimal dailyWithdrawalLimit;

    @Column(name = "minimum_balance", precision = 19, scale = 2)
    private BigDecimal minimumBalance;

    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    private BigDecimal overdraftLimit;

    // Custom fields stored as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_data", columnDefinition = "JSON")
    private Map<String, Object> customData;

    @PrePersist
    protected void onCreate() {
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
    }
}
