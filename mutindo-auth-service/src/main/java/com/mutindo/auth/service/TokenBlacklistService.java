package com.mutindo.auth.service;

import com.mutindo.logging.annotation.PerformanceLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token blacklist service implementation using Redis
 * Provides secure logout functionality by blacklisting JWT tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService implements ITokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String USER_TOKENS_PREFIX = "user:tokens:";
    private static final long DEFAULT_EXPIRY_HOURS = 24; // Tokens expire in 24 hours by default

    /**
     * Add token to blacklist
     */
    @Override
    @CacheEvict(value = "tokenValidation", key = "#token")
    public void blacklistToken(String token, Long userId, Long expiresAt) {
        log.debug("Blacklisting token for user: {}", userId);
        
        try {
            // Calculate TTL (Time To Live) for Redis
            long ttlSeconds = calculateTTL(expiresAt);
            
            // Store token in blacklist with TTL
            String blacklistKey = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, userId.toString(), ttlSeconds, TimeUnit.SECONDS);
            
            // Track user's tokens for bulk operations
            String userTokensKey = USER_TOKENS_PREFIX + userId;
            redisTemplate.opsForSet().add(userTokensKey, token);
            redisTemplate.expire(userTokensKey, ttlSeconds, TimeUnit.SECONDS);
            
            log.info("Token blacklisted successfully for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to blacklist token for user: {}", userId, e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * Check if token is blacklisted
     */
    @Override
    @Cacheable(value = "tokenValidation", key = "#token")
    @PerformanceLog
    public boolean isTokenBlacklisted(String token) {
        log.debug("Checking if token is blacklisted");
        
        try {
            String blacklistKey = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            
            boolean isBlacklisted = exists != null && exists;
            log.debug("Token blacklist check result: {}", isBlacklisted);
            
            return isBlacklisted;
            
        } catch (Exception e) {
            log.error("Failed to check token blacklist status", e);
            // In case of Redis failure, assume token is not blacklisted to avoid blocking valid users
            return false;
        }
    }

    /**
     * Remove token from blacklist (for testing purposes)
     */
    @Override
    @CacheEvict(value = "tokenValidation", key = "#token")
    public void removeFromBlacklist(String token) {
        log.debug("Removing token from blacklist");
        
        try {
            String blacklistKey = BLACKLIST_PREFIX + token;
            redisTemplate.delete(blacklistKey);
            
            log.info("Token removed from blacklist");
            
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist", e);
        }
    }

    /**
     * Blacklist all tokens for a user (force logout)
     */
    @Override
    @CacheEvict(value = "tokenValidation", allEntries = true)
    public void blacklistAllUserTokens(Long userId) {
        log.info("Blacklisting all tokens for user: {}", userId);
        
        try {
            String userTokensKey = USER_TOKENS_PREFIX + userId;
            Set<String> userTokens = redisTemplate.opsForSet().members(userTokensKey);
            
            if (userTokens != null && !userTokens.isEmpty()) {
                for (String token : userTokens) {
                    String blacklistKey = BLACKLIST_PREFIX + token;
                    redisTemplate.opsForValue().set(blacklistKey, userId.toString(), 
                        DEFAULT_EXPIRY_HOURS * 3600, TimeUnit.SECONDS);
                }
                
                // Clear user's token set
                redisTemplate.delete(userTokensKey);
                
                log.info("Blacklisted {} tokens for user: {}", userTokens.size(), userId);
            }
            
        } catch (Exception e) {
            log.error("Failed to blacklist all tokens for user: {}", userId, e);
            throw new RuntimeException("Failed to blacklist user tokens", e);
        }
    }

    /**
     * Clean up expired tokens from blacklist
     * Runs every hour to clean up expired entries
     */
    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        log.debug("Starting token blacklist cleanup");
        
        try {
            // Redis automatically handles TTL expiration, but we can add custom cleanup logic here
            // For example, we could scan for patterns and clean up manually if needed
            
            log.debug("Token blacklist cleanup completed");
            
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens", e);
        }
    }

    /**
     * Calculate TTL (Time To Live) for Redis storage
     */
    private long calculateTTL(Long expiresAt) {
        if (expiresAt == null) {
            return DEFAULT_EXPIRY_HOURS * 3600; // 24 hours in seconds
        }
        
        long currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long ttl = expiresAt - currentTime;
        
        // Ensure minimum TTL of 1 hour and maximum of 24 hours
        return Math.max(3600, Math.min(ttl, DEFAULT_EXPIRY_HOURS * 3600));
    }
}
