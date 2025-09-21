package com.mutindo.jwt.service;

import com.mutindo.jwt.dto.JwtClaims;
import com.mutindo.jwt.dto.TokenPair;

/**
 * JWT service interface for polymorphic JWT operations
 * Follows our established pattern of interface-driven design
 */
public interface IJwtService {
    
    /**
     * Generate access and refresh token pair
     * @param claims JWT claims to include in tokens
     * @return Token pair with access and refresh tokens
     */
    TokenPair generateTokenPair(JwtClaims claims);
    
    /**
     * Validate token and extract claims
     * @param token JWT token to validate
     * @return Extracted claims
     */
    JwtClaims validateAndExtractClaims(String token);
    
    /**
     * Check if token is expired
     * @param token JWT token to check
     * @return true if token is expired
     */
    boolean isTokenExpired(String token);
    
    /**
     * Refresh access token using refresh token
     * @param refreshToken Refresh token
     * @return New access token
     */
    String refreshAccessToken(String refreshToken);
}
