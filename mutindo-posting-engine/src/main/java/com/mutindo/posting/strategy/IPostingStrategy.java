package com.mutindo.posting.strategy;

import com.mutindo.posting.dto.PostingRequest;
import com.mutindo.posting.dto.PostingResult;

/**
 * Strategy interface for different posting types
 * Implements Strategy Pattern for polymorphic posting behavior
 */
public interface IPostingStrategy {
    
    /**
     * Execute the posting strategy
     */
    PostingResult executePosting(PostingRequest request);
    
    /**
     * Validate the posting request
     */
    void validateRequest(PostingRequest request);
    
    /**
     * Check if this strategy can handle the given posting type
     */
    boolean canHandle(String postingType);
    
    /**
     * Get the posting type this strategy handles
     */
    String getPostingType();
}
