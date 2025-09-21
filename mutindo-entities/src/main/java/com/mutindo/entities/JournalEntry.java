package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Journal Entry entity - double-entry bookkeeping header
 */
@Entity
@Table(name = "journal_entries", indexes = {
    @Index(name = "idx_je_posting_date", columnList = "postingDate"),
    @Index(name = "idx_je_source", columnList = "sourceType, sourceId"),
    @Index(name = "idx_je_idempotency", columnList = "idempotencyKey", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry extends BaseEntity {

    @NotNull
    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    @Size(max = 64)
    @Column(name = "source_type", length = 64)
    private String sourceType; // TRANSACTION, LOAN_DISBURSEMENT, etc.

    @Column(name = "source_id")
    private Long sourceId; // ID of the source transaction

    @Column(name = "narration", columnDefinition = "TEXT")
    private String narration;

    @Size(max = 64)
    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "reversed", nullable = false)
    private Boolean reversed = false;

    @Column(name = "reversal_entry_id")
    private Long reversalEntryId;
}
