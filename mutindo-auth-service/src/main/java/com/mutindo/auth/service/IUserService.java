package com.mutindo.auth.service;

import com.mutindo.auth.dto.UserDto;
import com.mutindo.auth.dto.UserRegistrationRequest;
import com.mutindo.auth.dto.UpdateUserRequest;
import com.mutindo.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * User service interface for user CRUD operations
 * Follows our established pattern of interface-driven design
 */
public interface IUserService {
    
    /**
     * Create new user
     * @param request User creation request
     * @return Created user information
     */
    UserDto createUser(UserRegistrationRequest request);
    
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
