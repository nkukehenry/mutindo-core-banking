package com.mutindo.posting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Posting request DTO
 */
@Data
@Builder(toBuilder = true)
public class PostingRequest {
    
    private String idempotencyKey;
    private String postingType; // DEPOSIT, WITHDRAWAL, TRANSFER, LOAN_DISBURSEMENT, etc.
    private String sourceType; // TRANSACTION, LOAN, ADJUSTMENT, etc.
    private Long sourceId;
    private Long branchId;
    private Long userId;
    private LocalDate postingDate;
    private String narration;
    private String currency;
    private List<PostingEntry> entries;
    
    @Data
    @Builder
    public static class PostingEntry {
        private String glAccountCode;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private String narration;
        private Long branchId;
    }
}
