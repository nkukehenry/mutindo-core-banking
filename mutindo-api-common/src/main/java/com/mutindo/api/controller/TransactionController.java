package com.mutindo.api.controller;

import com.mutindo.common.context.BranchContextHolder; // Real context access
import com.mutindo.common.dto.BaseResponse; // Reusing existing response wrapper
import com.mutindo.logging.annotation.AuditLog; // Reusing existing audit logging
import com.mutindo.logging.annotation.PerformanceLog; // Reusing existing performance logging
import com.mutindo.posting.dto.PostingRequest; // Reusing existing posting DTOs
import com.mutindo.posting.dto.PostingResult; // Reusing existing posting DTOs
import com.mutindo.posting.service.IPostingEngine; // Reusing existing posting service interface
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Transaction REST API controller for testing posting engine
 * Reuses existing posting engine and response infrastructure
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction processing operations")
public class TransactionController {

    private final IPostingEngine postingEngine; // Reusing existing posting service interface

    /**
     * Process deposit transaction
     */
    @PostMapping("/deposits")
    @Operation(summary = "Process deposit", description = "Process cash deposit transaction")
    @PreAuthorize("hasRole('ROLE_TRANSACTIONS_CREATE')") // Security check
    @AuditLog // Reusing existing audit logging
    @PerformanceLog // Reusing existing performance logging
    public ResponseEntity<BaseResponse<PostingResult>> processDeposit(@Valid @RequestBody DepositRequest request) {
        log.info("Processing deposit via API - Account: {} - Amount: {}", 
                request.getAccountNumber(), request.getAmount());

        try {
            // Build posting request for deposit (small method)
            PostingRequest postingRequest = buildDepositPostingRequest(request);
            
            // Use existing posting engine
            PostingResult result = postingEngine.postTransaction(postingRequest);
            
            if (result.isSuccess()) {
                log.info("Deposit processed successfully via API - Journal Entry: {}", result.getJournalEntryId());
                return ResponseEntity.ok(BaseResponse.success(result, "Deposit processed successfully"));
            } else {
                log.warn("Deposit processing failed via API: {}", result.getMessage());
                return ResponseEntity.status(400)
                        .body(BaseResponse.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("Failed to process deposit via API", e);
            throw e; // Let global exception handler manage the response
        }
    }

    /**
     * Process withdrawal transaction
     */
    @PostMapping("/withdrawals")
    @Operation(summary = "Process withdrawal", description = "Process cash withdrawal transaction")
    @PreAuthorize("hasRole('ROLE_TRANSACTIONS_CREATE')") // Security check
    @AuditLog
    @PerformanceLog
    public ResponseEntity<BaseResponse<PostingResult>> processWithdrawal(@Valid @RequestBody WithdrawalRequest request) {
        log.info("Processing withdrawal via API - Account: {} - Amount: {}", 
                request.getAccountNumber(), request.getAmount());

        try {
            // Build posting request for withdrawal (small method)
            PostingRequest postingRequest = buildWithdrawalPostingRequest(request);
            
            // Use existing posting engine
            PostingResult result = postingEngine.postTransaction(postingRequest);
            
            if (result.isSuccess()) {
                log.info("Withdrawal processed successfully via API - Journal Entry: {}", result.getJournalEntryId());
                return ResponseEntity.ok(BaseResponse.success(result, "Withdrawal processed successfully"));
            } else {
                log.warn("Withdrawal processing failed via API: {}", result.getMessage());
                return ResponseEntity.status(400)
                        .body(BaseResponse.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("Failed to process withdrawal via API", e);
            throw e;
        }
    }

    /**
     * Test posting engine directly (for development/testing)
     */
    @PostMapping("/test-posting")
    @Operation(summary = "Test posting", description = "Direct posting engine test endpoint")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Admin only
    @AuditLog
    public ResponseEntity<BaseResponse<PostingResult>> testPosting(@Valid @RequestBody PostingRequest request) {
        log.info("Testing posting engine via API - Type: {}", request.getPostingType());

        try {
            // Use existing posting engine directly
            PostingResult result = postingEngine.postTransaction(request);
            
            log.info("Posting test completed via API - Success: {}", result.isSuccess());
            return ResponseEntity.ok(BaseResponse.success(result));
            
        } catch (Exception e) {
            log.error("Failed to test posting via API", e);
            throw e;
        }
    }

    // Private helper methods (small and focused)

    /**
     * Build posting request for deposit transaction
     */
    private PostingRequest buildDepositPostingRequest(DepositRequest request) {
        return PostingRequest.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .postingType("DEPOSIT")
                .sourceType("TRANSACTION")
                .sourceId(generateTransactionId()) // Generate unique transaction ID
                .branchId(getCurrentBranchId())
                .userId(getCurrentUserId())
                .postingDate(LocalDate.now())
                .narration(request.getNarration())
                .currency(request.getCurrency())
                .entries(List.of(
                        PostingRequest.PostingEntry.builder()
                                .glAccountCode("1010") // Cash account
                                .debitAmount(request.getAmount())
                                .creditAmount(BigDecimal.ZERO)
                                .narration("Cash deposit")
                                .branchId(getCurrentBranchId())
                                .build()
                ))
                .build();
    }

    /**
     * Build posting request for withdrawal transaction
     */
    private PostingRequest buildWithdrawalPostingRequest(WithdrawalRequest request) {
        return PostingRequest.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .postingType("WITHDRAWAL")
                .sourceType("TRANSACTION")
                .sourceId(generateTransactionId()) // Generate unique transaction ID
                .branchId(getCurrentBranchId())
                .userId(getCurrentUserId())
                .postingDate(LocalDate.now())
                .narration(request.getNarration())
                .currency(request.getCurrency())
                .entries(List.of(
                        PostingRequest.PostingEntry.builder()
                                .glAccountCode("2010") // Customer deposits
                                .debitAmount(BigDecimal.ZERO)
                                .creditAmount(request.getAmount())
                                .narration("Cash withdrawal")
                                .branchId(getCurrentBranchId())
                                .build()
                ))
                .build();
    }

    /**
     * Get current branch ID from context
     */
    private Long getCurrentBranchId() {
        Long branchId = BranchContextHolder.getCurrentBranchId();
        if (branchId == null) {
            log.warn("No branch context found, using default branch");
            return 1L; // Default to main branch if no context (should not happen in production)
        }
        return branchId;
    }

    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        Long userId = BranchContextHolder.getCurrentUserId();
        if (userId == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.warn("No user context found, authenticated user: {}", auth != null ? auth.getName() : "anonymous");
            return 1L; // Default to system user if no context (should not happen in production)
        }
        return userId;
    }

    /**
     * Generate unique transaction ID for posting
     */
    private Long generateTransactionId() {
        // In production, this would be generated by a transaction service
        // For now, use timestamp-based ID
        return System.currentTimeMillis();
    }

    // Request DTOs for transaction endpoints

    @Data
    @Builder
    public static class DepositRequest {
        private String accountNumber;
        private BigDecimal amount;
        private String currency;
        private String narration;
    }

    @Data
    @Builder
    public static class WithdrawalRequest {
        private String accountNumber;
        private BigDecimal amount;
        private String currency;
        private String narration;
    }
}
