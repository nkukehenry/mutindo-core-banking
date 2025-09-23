package com.mutindo.api.controller;

import com.mutindo.auth.service.IAuthenticationService;
import com.mutindo.auth.dto.LoginRequest;
import com.mutindo.auth.dto.LoginResponse;
import com.mutindo.auth.dto.RefreshTokenRequest;
import com.mutindo.auth.dto.UserRegistrationRequest;
import com.mutindo.auth.dto.UserDto;
import com.mutindo.auth.dto.ChangePasswordRequest;
import com.mutindo.common.dto.BaseResponse;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST API controller
 * Complete authentication and user management operations
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and user management operations")
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    /**
     * User login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with username/email and password")
    @PerformanceLog
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt via API - Username: {}", request.getUsername());

        try {
            LoginResponse response = authenticationService.authenticateUser(request);
            
            log.info("User login successful via API - User: {}", request.getUsername());
            return ResponseEntity.ok(BaseResponse.success(response, "Login successful"));
            
        } catch (Exception e) {
            log.error("Failed to login user via API: {}", request.getUsername(), e);
            throw e;
        }
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    @PerformanceLog
    public ResponseEntity<BaseResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh attempt via API");

        try {
            LoginResponse response = authenticationService.refreshAccessToken(request);
            
            log.debug("Token refresh successful via API");
            return ResponseEntity.ok(BaseResponse.success(response, "Token refreshed successfully"));
            
        } catch (Exception e) {
            log.error("Failed to refresh token via API", e);
            throw e;
        }
    }

    /**
     * Register new user (admin operation)
     */
    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register a new system user (admin only)")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    @AuditLog(action = "REGISTER_USER", entity = "User")
    @PerformanceLog
    public ResponseEntity<BaseResponse<UserDto>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("User registration via API - Username: {} - Type: {}", request.getUsername(), request.getUserType());

        try {
            UserDto user = authenticationService.registerUser(request);
            
            log.info("User registered successfully via API: {}", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(user, "User registered successfully"));
            
        } catch (Exception e) {
            log.error("Failed to register user via API", e);
            throw e;
        }
    }

    /**
     * Change user password
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    @PreAuthorize("hasRole('ROLE_USERS_UPDATE') or #userId == authentication.name")
    @AuditLog(action = "CHANGE_PASSWORD", entity = "User")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @RequestParam Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request via API - User: {}", userId);

        try {
            authenticationService.changePassword(userId, request);
            
            log.info("Password changed successfully via API - User: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "Password changed successfully"));
            
        } catch (Exception e) {
            log.error("Failed to change password via API - User: {}", userId, e);
            throw e;
        }
    }

    /**
     * Lock user account
     */
    @PostMapping("/lock-user")
    @Operation(summary = "Lock user", description = "Lock user account (security operation)")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SECURITY')")
    @AuditLog(action = "LOCK_USER", entity = "User")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Void>> lockUser(
            @RequestParam Long userId,
            @RequestParam String reason) {
        log.info("User lock request via API - User: {} - Reason: {}", userId, reason);

        try {
            authenticationService.lockUser(userId, reason);
            
            log.info("User locked successfully via API - User: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "User locked successfully"));
            
        } catch (Exception e) {
            log.error("Failed to lock user via API - User: {}", userId, e);
            throw e;
        }
    }

    /**
     * Unlock user account
     */
    @PostMapping("/unlock-user")
    @Operation(summary = "Unlock user", description = "Unlock user account")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SECURITY')")
    @AuditLog(action = "UNLOCK_USER", entity = "User")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Void>> unlockUser(@RequestParam Long userId) {
        log.info("User unlock request via API - User: {}", userId);

        try {
            authenticationService.unlockUser(userId);
            
            log.info("User unlocked successfully via API - User: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "User unlocked successfully"));
            
        } catch (Exception e) {
            log.error("Failed to unlock user via API - User: {}", userId, e);
            throw e;
        }
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout user and invalidate tokens")
    @AuditLog(action = "LOGOUT_USER", entity = "User")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Void>> logout(
            @RequestParam Long userId,
            @RequestParam String accessToken) {
        log.info("User logout request via API - User: {}", userId);

        try {
            authenticationService.logoutUser(userId, accessToken);
            
            log.info("User logged out successfully via API - User: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "Logout successful"));
            
        } catch (Exception e) {
            log.error("Failed to logout user via API - User: {}", userId, e);
            throw e;
        }
    }

    /**
     * Validate user authentication
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate user", description = "Validate if user is authenticated and active")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Boolean>> validateUser(@RequestParam Long userId) {
        log.debug("User validation request via API - User: {}", userId);

        try {
            boolean isValid = authenticationService.validateUser(userId);
            
            log.debug("User validation completed via API - User: {} - Valid: {}", userId, isValid);
            return ResponseEntity.ok(BaseResponse.success(isValid, "User validation completed"));
            
        } catch (Exception e) {
            log.error("Failed to validate user via API - User: {}", userId, e);
            throw e;
        }
    }
}
