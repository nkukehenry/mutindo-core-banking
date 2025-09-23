package com.mutindo.api.controller;

import com.mutindo.posting.service.IPostingEngine;
import com.mutindo.posting.dto.PostingRequest;
import com.mutindo.posting.dto.PostingResult;
import com.mutindo.posting.dto.ReversalRequest;
import com.mutindo.common.dto.BaseResponse;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Posting Engine REST API controller
 * Complete double-entry posting and transaction reversal operations
 */
@RestController
@RequestMapping("/api/v1/posting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Posting Engine", description = "Double-entry posting and transaction reversal operations")
public class PostingController {

    private final IPostingEngine postingEngine;

    /**
     * Post transaction synchronously
     */
    @PostMapping("/post")
    @Operation(summary = "Post transaction", description = "Post a transaction using double-entry bookkeeping")
    @PreAuthorize("hasRole('ROLE_TELLER') or hasRole('ROLE_BRANCH_MANAGER') or hasRole('ROLE_ADMIN')")
    @AuditLog(action = "POST_TRANSACTION", entity = "Transaction")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PostingResult>> postTransaction(@Valid @RequestBody PostingRequest request) {
        log.info("Transaction posting request via API - Type: {} - Amount: {} - Reference: {}", 
                request.getTransactionType(), request.getAmount(), request.getReference());

        try {
            // Validate posting request
            postingEngine.validatePostingRequest(request);
            
            // Check idempotency
            if (postingEngine.isAlreadyProcessed(request.getIdempotencyKey())) {
                log.warn("Duplicate posting request detected - Key: {}", request.getIdempotencyKey());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(BaseResponse.error("Transaction already processed"));
            }
            
            PostingResult result = postingEngine.postTransaction(request);
            
            log.info("Transaction posted successfully via API - Reference: {} - Result: {}", 
                    request.getReference(), result.getStatus());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(result, "Transaction posted successfully"));
            
        } catch (Exception e) {
            log.error("Failed to post transaction via API - Reference: {}", request.getReference(), e);
            throw e;
        }
    }

    /**
     * Post transaction asynchronously
     */
    @PostMapping("/post-async")
    @Operation(summary = "Post transaction async", description = "Post a transaction asynchronously")
    @PreAuthorize("hasRole('ROLE_TELLER') or hasRole('ROLE_BRANCH_MANAGER') or hasRole('ROLE_ADMIN')")
    @AuditLog(action = "POST_TRANSACTION_ASYNC", entity = "Transaction")
    @PerformanceLog
    public ResponseEntity<BaseResponse<String>> postTransactionAsync(@Valid @RequestBody PostingRequest request) {
        log.info("Async transaction posting request via API - Type: {} - Amount: {} - Reference: {}", 
                request.getTransactionType(), request.getAmount(), request.getReference());

        try {
            // Validate posting request
            postingEngine.validatePostingRequest(request);
            
            // Check idempotency
            if (postingEngine.isAlreadyProcessed(request.getIdempotencyKey())) {
                log.warn("Duplicate async posting request detected - Key: {}", request.getIdempotencyKey());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(BaseResponse.error("Transaction already processed"));
            }
            
            CompletableFuture<PostingResult> future = postingEngine.postTransactionAsync(request);
            
            log.info("Async transaction posting initiated via API - Reference: {}", request.getReference());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(BaseResponse.success("Transaction posting initiated", "Transaction is being processed asynchronously"));
            
        } catch (Exception e) {
            log.error("Failed to initiate async transaction posting via API - Reference: {}", request.getReference(), e);
            throw e;
        }
    }

    /**
     * Reverse transaction
     */
    @PostMapping("/reverse")
    @Operation(summary = "Reverse transaction", description = "Reverse a previously posted transaction")
    @PreAuthorize("hasRole('ROLE_BRANCH_MANAGER') or hasRole('ROLE_ADMIN')")
    @AuditLog(action = "REVERSE_TRANSACTION", entity = "Transaction")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PostingResult>> reverseTransaction(@Valid @RequestBody ReversalRequest request) {
        log.info("Transaction reversal request via API - Original Reference: {} - Reason: {}", 
                request.getOriginalReference(), request.getReason());

        try {
            PostingResult result = postingEngine.reverseTransaction(request);
            
            log.info("Transaction reversed successfully via API - Original Reference: {} - Result: {}", 
                    request.getOriginalReference(), result.getStatus());
            return ResponseEntity.ok(BaseResponse.success(result, "Transaction reversed successfully"));
            
        } catch (Exception e) {
            log.error("Failed to reverse transaction via API - Original Reference: {}", request.getOriginalReference(), e);
            throw e;
        }
    }

    /**
     * Validate posting request
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate posting request", description = "Validate a posting request before processing")
    @PreAuthorize("hasRole('ROLE_TELLER') or hasRole('ROLE_BRANCH_MANAGER') or hasRole('ROLE_ADMIN')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Void>> validatePostingRequest(@Valid @RequestBody PostingRequest request) {
        log.debug("Posting request validation via API - Type: {} - Reference: {}", 
                request.getTransactionType(), request.getReference());

        try {
            postingEngine.validatePostingRequest(request);
            
            log.debug("Posting request validation successful via API - Reference: {}", request.getReference());
            return ResponseEntity.ok(BaseResponse.success(null, "Posting request is valid"));
            
        } catch (Exception e) {
            log.error("Posting request validation failed via API - Reference: {}", request.getReference(), e);
            throw e;
        }
    }

    /**
     * Check idempotency
     */
    @GetMapping("/check-idempotency")
    @Operation(summary = "Check idempotency", description = "Check if a transaction with given idempotency key was already processed")
    @PreAuthorize("hasRole('ROLE_TELLER') or hasRole('ROLE_BRANCH_MANAGER') or hasRole('ROLE_ADMIN')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Boolean>> checkIdempotency(@RequestParam String idempotencyKey) {
        log.debug("Idempotency check via API - Key: {}", idempotencyKey);

        try {
            boolean isProcessed = postingEngine.isAlreadyProcessed(idempotencyKey);
            
            log.debug("Idempotency check completed via API - Key: {} - Processed: {}", idempotencyKey, isProcessed);
            return ResponseEntity.ok(BaseResponse.success(isProcessed, "Idempotency check completed"));
            
        } catch (Exception e) {
            log.error("Failed to check idempotency via API - Key: {}", idempotencyKey, e);
            throw e;
        }
    }
}
