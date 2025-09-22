package com.mutindo.auth.service;

/**
 * Token blacklist service interface for logout functionality
 */
public interface ITokenBlacklistService {
    
    /**
     * Add token to blacklist
     * @param token JWT token to blacklist
     * @param userId User ID who owns the token
     * @param expiresAt Token expiration time
     */
    void blacklistToken(String token, Long userId, Long expiresAt);
    
    /**
     * Check if token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted
     */
    boolean isTokenBlacklisted(String token);
    
    /**
     * Remove token from blacklist (for testing purposes)
     * @param token JWT token to remove
     */
    void removeFromBlacklist(String token);
    
    /**
     * Blacklist all tokens for a user (force logout)
     * @param userId User ID
     */
    void blacklistAllUserTokens(Long userId);
    
    /**
     * Clean up expired tokens from blacklist
     */
    void cleanupExpiredTokens();
}
