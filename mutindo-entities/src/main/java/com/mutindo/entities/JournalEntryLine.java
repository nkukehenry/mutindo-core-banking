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
 * Journal Entry Line entity - individual debit/credit entries
 */
@Entity
@Table(name = "journal_entry_lines", indexes = {
    @Index(name = "idx_jel_entry", columnList = "journalEntryId"),
    @Index(name = "idx_jel_gl_account", columnList = "glAccountCode"),
    @Index(name = "idx_jel_branch", columnList = "branchId")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryLine extends BaseEntity {

    @NotBlank
    @NotNull
    @Column(name = "journal_entry_id", nullable = false)
    private Long journalEntryId;

    @NotBlank
    @Size(max = 32)
    @Column(name = "gl_account_code", nullable = false, length = 32)
    private String glAccountCode;

    @NotNull
    @Column(name = "debit", nullable = false, precision = 19, scale = 2)
    private BigDecimal debit = BigDecimal.ZERO;

    @NotNull
    @Column(name = "credit", nullable = false, precision = 19, scale = 2)
    private BigDecimal credit = BigDecimal.ZERO;

    @Column(name = "narration", length = 512)
    private String narration;

    @NotNull
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Size(max = 8)
    @Column(name = "currency", length = 8)
    private String currency;

    // For multi-currency support
    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "base_currency_debit", precision = 19, scale = 2)
    private BigDecimal baseCurrencyDebit;

    @Column(name = "base_currency_credit", precision = 19, scale = 2)
    private BigDecimal baseCurrencyCredit;
}
