package com.mutindo.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.jwt.dto.JwtClaims;
import com.mutindo.jwt.dto.TokenPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT service implementation - focused only on JWT token operations
 * Follows our established interface-driven pattern
 */
@Service
@Slf4j
public class JwtService implements IJwtService {

    @Value("${cbs.jwt.secret}")
    private String secret;

    @Value("${cbs.jwt.issuer:cbs}")
    private String issuer;

    @Value("${cbs.jwt.access-token-expiry:15}")
    private int accessTokenExpiryMinutes;

    @Value("${cbs.jwt.refresh-token-expiry:7}")
    private int refreshTokenExpiryDays;

    public TokenPair generateTokenPair(JwtClaims claims) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            
            String accessToken = JWT.create()
                    .withIssuer(issuer)
                    .withSubject(claims.getUserId())
                    .withClaim("branchId", claims.getBranchId())
                    .withClaim("userType", claims.getUserType())
                    .withClaim("roles", claims.getRoles())
                    .withClaim("permissions", claims.getPermissions())
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(Instant.now().plus(accessTokenExpiryMinutes, ChronoUnit.MINUTES)))
                    .sign(algorithm);

            String refreshToken = JWT.create()
                    .withIssuer(issuer)
                    .withSubject(claims.getUserId())
                    .withClaim("tokenType", "REFRESH")
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(Instant.now().plus(refreshTokenExpiryDays, ChronoUnit.DAYS)))
                    .sign(algorithm);

            return TokenPair.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(accessTokenExpiryMinutes * 60) // seconds
                    .build();

        } catch (JWTCreationException e) {
            log.error("Failed to create JWT token", e);
            throw new BusinessException("Token generation failed", "JWT_CREATION_ERROR");
        }
    }

    public JwtClaims validateAndExtractClaims(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();

            DecodedJWT jwt = verifier.verify(token);

            return JwtClaims.builder()
                    .userId(jwt.getSubject())
                    .branchId(jwt.getClaim("branchId").asString())
                    .userType(jwt.getClaim("userType").asString())
                    .roles(jwt.getClaim("roles").asList(String.class))
                    .permissions(jwt.getClaim("permissions").asList(String.class))
                    .issuedAt(jwt.getIssuedAt().toInstant())
                    .expiresAt(jwt.getExpiresAt().toInstant())
                    .build();

        } catch (JWTVerificationException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw new BusinessException("Invalid or expired token", "JWT_VALIDATION_ERROR");
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String refreshAccessToken(String refreshToken) {
        JwtClaims claims = validateAndExtractClaims(refreshToken);
        
        // Verify it's a refresh token
        DecodedJWT jwt = JWT.decode(refreshToken);
        String tokenType = jwt.getClaim("tokenType").asString();
        if (!"REFRESH".equals(tokenType)) {
            throw new BusinessException("Invalid refresh token", "INVALID_REFRESH_TOKEN");
        }

        // Generate new access token with same claims
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(claims.getUserId())
                .withClaim("branchId", claims.getBranchId())
                .withClaim("userType", claims.getUserType())
                .withClaim("roles", claims.getRoles())
                .withClaim("permissions", claims.getPermissions())
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(accessTokenExpiryMinutes, ChronoUnit.MINUTES)))
                .sign(algorithm);
    }
}
