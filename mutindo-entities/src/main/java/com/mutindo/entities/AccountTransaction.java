package com.mutindo.entities;

import com.mutindo.common.enums.TransactionStatus;
import com.mutindo.common.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account transaction entity - transaction header
 */
@Entity
@Table(name = "account_transactions", indexes = {
    @Index(name = "idx_tx_account", columnList = "accountId"),
    @Index(name = "idx_tx_status", columnList = "status"),
    @Index(name = "idx_tx_type", columnList = "txType"),
    @Index(name = "idx_tx_idempotency", columnList = "idempotencyKey", unique = true),
    @Index(name = "idx_tx_posted_date", columnList = "postedAt"),
    @Index(name = "idx_tx_reference", columnList = "reference")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransaction extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 32)
    private TransactionType txType;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 8)
    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Size(max = 128)
    @Column(name = "reference", length = 128)
    private String reference;

    @Size(max = 64)
    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    @Column(name = "narration", columnDefinition = "TEXT")
    private String narration;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Size(max = 36)
    @Column(name = "related_account_id", length = 36) // For transfers
    private String relatedAccountId;

    @Size(max = 32)
    @Column(name = "channel", length = 32) // TELLER, ATM, ONLINE, MOBILE, etc.
    private String channel;

    @Size(max = 64)
    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "balance_before", precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;
}
