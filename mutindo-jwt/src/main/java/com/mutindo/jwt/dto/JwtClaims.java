package com.mutindo.jwt.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * JWT claims DTO - focused on token payload
 */
@Data
@Builder
public class JwtClaims {
    private String userId;
    private String branchId; // null for institution admins
    private String userType;
    private List<String> roles;
    private List<String> permissions;
    private Instant issuedAt;
    private Instant expiresAt;
}
