package com.mutindo.auth.service;

import com.mutindo.auth.dto.LoginRequest;
import com.mutindo.auth.dto.LoginResponse;
import com.mutindo.auth.dto.RefreshTokenRequest;
import com.mutindo.auth.dto.UserRegistrationRequest;
import com.mutindo.auth.dto.UserDto;
import com.mutindo.auth.dto.ChangePasswordRequest;

/**
 * Authentication service interface for polymorphic authentication operations
 * Follows our established pattern of interface-driven design
 */
public interface IAuthenticationService {
    
    /**
     * Authenticate user with username/email and password
     * @param request Login credentials
     * @return Login response with JWT tokens and user info
     */
    LoginResponse authenticateUser(LoginRequest request);
    
    /**
     * Refresh access token using refresh token
     * @param request Refresh token request
     * @return New access token
     */
    LoginResponse refreshAccessToken(RefreshTokenRequest request);
    
    /**
     * Register new user (admin operation)
     * @param request User registration details
     * @return Created user information
     */
    UserDto registerUser(UserRegistrationRequest request);
    
    /**
     * Change user password
     * @param userId User ID
     * @param request Password change request
     */
    void changePassword(Long userId, ChangePasswordRequest request);
    
    /**
     * Lock user account (security operation)
     * @param userId User ID to lock
     * @param reason Lock reason
     */
    void lockUser(Long userId, String reason);
    
    /**
     * Unlock user account
     * @param userId User ID to unlock
     */
    void unlockUser(Long userId);
    
    /**
     * Logout user (invalidate tokens)
     * @param userId User ID
     * @param accessToken Access token to invalidate
     */
    void logoutUser(Long userId, String accessToken);
    
    /**
     * Validate if user is authenticated and active
     * @param userId User ID to validate
     * @return true if user is valid and active
     */
    boolean isUserValid(Long userId);
}
