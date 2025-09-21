package com.mutindo.api.controller;

import com.mutindo.auth.dto.LoginRequest; // Reusing existing DTOs
import com.mutindo.auth.dto.LoginResponse; // Reusing existing DTOs
import com.mutindo.auth.dto.RefreshTokenRequest; // Reusing existing DTOs
import com.mutindo.auth.service.IAuthenticationService; // Reusing existing service interface
import com.mutindo.common.dto.BaseResponse; // Reusing existing response wrapper
import com.mutindo.logging.annotation.PerformanceLog; // Reusing existing performance logging
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST API controller
 * Reuses existing authentication service and response infrastructure
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and token management")
public class AuthController {

    private final IAuthenticationService authenticationService; // Reusing existing service interface

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @PerformanceLog // Reusing existing performance logging
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());

        try {
            // Use existing authentication service
            LoginResponse loginResponse = authenticationService.authenticateUser(request);
            
            if (loginResponse.isSuccess()) {
                log.info("Login successful for user: {}", request.getUsernameOrEmail());
                return ResponseEntity.ok(BaseResponse.success(loginResponse, "Login successful"));
            } else {
                log.warn("Login failed for user: {}", request.getUsernameOrEmail());
                return ResponseEntity.status(401)
                        .body(BaseResponse.error(loginResponse.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("Login error for user: {}", request.getUsernameOrEmail(), e);
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("Login failed: " + e.getMessage()));
        }
    }

    /**
     * Refresh access token endpoint
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    @PerformanceLog
    public ResponseEntity<BaseResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");

        try {
            // Use existing authentication service
            LoginResponse refreshResponse = authenticationService.refreshAccessToken(request);
            
            log.info("Token refreshed successfully");
            return ResponseEntity.ok(BaseResponse.success(refreshResponse, "Token refreshed successfully"));
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate tokens")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("Logout request");

        try {
            // Extract token from header (small method)
            String token = extractToken(authorizationHeader);
            
            // Get current user ID from security context
            String userId = getCurrentUserId();
            
            // Convert String ID to Long for service call
            Long userIdLong = Long.parseLong(userId);
            
            // Use existing authentication service
            authenticationService.logoutUser(userIdLong, token);
            
            log.info("User logged out successfully: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "Logout successful"));
            
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("Logout failed: " + e.getMessage()));
        }
    }

    // Private helper methods (small and focused)

    /**
     * Extract token from Authorization header
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }

    /**
     * Get current user ID from security context
     */
    private String getCurrentUserId() {
        // This would extract from Spring Security context
        // For now, return a placeholder
        return "current-user-id";
    }
}
