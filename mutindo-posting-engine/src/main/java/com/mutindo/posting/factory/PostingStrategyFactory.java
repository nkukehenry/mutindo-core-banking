package com.mutindo.posting.factory;

import com.mutindo.exceptions.BusinessException;
import com.mutindo.posting.strategy.IPostingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory implementation for posting strategies
 * Implements Factory Pattern with automatic strategy registration
 */
@Component
@Slf4j
public class PostingStrategyFactory implements IPostingStrategyFactory {
    
    private final Map<String, IPostingStrategy> strategies = new ConcurrentHashMap<>();
    private final List<IPostingStrategy> postingStrategies;
    
    public PostingStrategyFactory(List<IPostingStrategy> postingStrategies) {
        this.postingStrategies = postingStrategies;
    }
    
    @PostConstruct
    public void initializeStrategies() {
        log.info("Initializing posting strategies");
        
        for (IPostingStrategy strategy : postingStrategies) {
            registerStrategy(strategy);
        }
        
        log.info("Registered {} posting strategies", strategies.size());
    }
    
    @Override
    public IPostingStrategy getPostingStrategy(String postingType) {
        IPostingStrategy strategy = strategies.get(postingType);
        
        if (strategy == null) {
            throw new BusinessException(
                "No posting strategy found for type: " + postingType, 
                "POSTING_STRATEGY_NOT_FOUND"
            );
        }
        
        return strategy;
    }
    
    @Override
    public void registerStrategy(IPostingStrategy strategy) {
        String postingType = strategy.getPostingType();
        strategies.put(postingType, strategy);
        
        log.info("Registered posting strategy: {} -> {}", 
                postingType, strategy.getClass().getSimpleName());
    }
    
    @Override
    public boolean hasStrategy(String postingType) {
        return strategies.containsKey(postingType);
    }
}
