package com.mutindo.posting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Posting result DTO
 */
@Data
@Builder
public class PostingResult {
    
    private boolean success;
    private Long journalEntryId;
    private String message;
    private String errorCode;
    private LocalDateTime postedAt;
    private String idempotencyKey;
    
    public static PostingResult success(Long journalEntryId, String idempotencyKey) {
        return PostingResult.builder()
                .success(true)
                .journalEntryId(journalEntryId)
                .idempotencyKey(idempotencyKey)
                .postedAt(LocalDateTime.now())
                .message("Posted successfully")
                .build();
    }
    
    public static PostingResult failure(String errorCode, String message, String idempotencyKey) {
        return PostingResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .idempotencyKey(idempotencyKey)
                .build();
    }
}
