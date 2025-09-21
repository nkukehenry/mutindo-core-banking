package com.mutindo.posting.factory;

import com.mutindo.posting.strategy.IPostingStrategy;

/**
 * Factory interface for creating posting strategies
 * Implements Factory Pattern for strategy selection
 */
public interface IPostingStrategyFactory {
    
    /**
     * Get appropriate posting strategy for the given posting type
     */
    IPostingStrategy getPostingStrategy(String postingType);
    
    /**
     * Register a new posting strategy
     */
    void registerStrategy(IPostingStrategy strategy);
    
    /**
     * Check if strategy exists for posting type
     */
    boolean hasStrategy(String postingType);
}
