package com.mutindo.posting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Reversal request DTO
 */
@Data
@Builder
public class ReversalRequest {
    
    private String idempotencyKey;
    private Long originalJournalEntryId;
    private String reversalReason;
    private LocalDate reversalDate;
    private Long userId;
}
