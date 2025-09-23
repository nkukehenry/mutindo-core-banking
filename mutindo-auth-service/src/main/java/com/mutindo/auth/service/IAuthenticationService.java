package com.mutindo.auth.service;

import com.mutindo.auth.dto.LoginRequest;
import com.mutindo.auth.dto.LoginResponse;
import com.mutindo.auth.dto.RefreshTokenRequest;
import com.mutindo.auth.dto.UserRegistrationRequest;
import com.mutindo.auth.dto.UserDto;
import com.mutindo.auth.dto.ChangePasswordRequest;
import com.mutindo.auth.dto.UpdateUserRequest;
import com.mutindo.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

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
    
    /**
     * Force logout user from all sessions (security operation)
     * @param userId User ID to force logout
     */
    void forceLogoutUser(Long userId);
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User information if found
     */
    Optional<UserDto> getUserById(Long userId);
    
    /**
     * Get user by username
     * @param username Username
     * @return User information if found
     */
    Optional<UserDto> getUserByUsername(String username);
    
    /**
     * Update user information
     * @param userId User ID
     * @param request Update request
     * @return Updated user information
     */
    UserDto updateUser(Long userId, UpdateUserRequest request);
    
    /**
     * Get all users with pagination and filtering
     * @param userType User type filter
     * @param branchId Branch ID filter
     * @param active Active status filter
     * @param pageable Pagination parameters
     * @return Paginated user results
     */
    PaginatedResponse<UserDto> getAllUsers(String userType, String branchId, Boolean active, Pageable pageable);
    
    /**
     * Search users with pagination
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Paginated user results
     */
    PaginatedResponse<UserDto> searchUsers(String searchTerm, Pageable pageable);
    
    /**
     * Deactivate user (soft delete)
     * @param userId User ID
     * @param reason Deactivation reason
     */
    void deactivateUser(Long userId, String reason);
    
    /**
     * Activate user
     * @param userId User ID
     */
    void activateUser(Long userId);
}
