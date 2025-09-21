package com.mutindo.posting.service;

import com.mutindo.posting.dto.PostingRequest;
import com.mutindo.posting.dto.PostingResult;
import com.mutindo.posting.dto.ReversalRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Posting Engine interface for polymorphic double-entry posting
 */
public interface IPostingEngine {
    
    /**
     * Post a transaction synchronously
     */
    PostingResult postTransaction(PostingRequest request);
    
    /**
     * Post a transaction asynchronously
     */
    CompletableFuture<PostingResult> postTransactionAsync(PostingRequest request);
    
    /**
     * Reverse a posted transaction
     */
    PostingResult reverseTransaction(ReversalRequest request);
    
    /**
     * Validate posting request before processing
     */
    void validatePostingRequest(PostingRequest request);
    
    /**
     * Check if idempotency key already processed
     */
    boolean isAlreadyProcessed(String idempotencyKey);
}
