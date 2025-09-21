package com.mutindo.jwt.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Token pair DTO - access and refresh tokens
 */
@Data
@Builder
public class TokenPair {
    private String accessToken;
    private String refreshToken;
    private int expiresIn; // Access token expiry in seconds
    private String tokenType = "Bearer";
}
