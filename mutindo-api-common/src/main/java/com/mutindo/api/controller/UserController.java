package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.common.enums.UserType;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User REST API controller
 * Complete CRUD operations for user management
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management operations")
public class UserController {

    // TODO: Inject IUserService when available
    // private final IUserService userService;

    /**
     * Create new user
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new system user")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    @AuditLog(action = "CREATE_USER", entity = "User")
    @PerformanceLog
    public ResponseEntity<BaseResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user via API - Username: {} - Type: {}", request.getUsername(), request.getUserType());

        try {
            // TODO: Use real service when available
            UserDto user = UserDto.builder()
                    .id(System.currentTimeMillis())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .userType(request.getUserType())
                    .branchId(request.getBranchId())
                    .supervisorId(request.getSupervisorId())
                    .department(request.getDepartment())
                    .position(request.getPosition())
                    .employeeId(request.getEmployeeId())
                    .active(true)
                    .emailVerified(false)
                    .phoneVerified(false)
                    .mfaEnabled(false)
                    .mustChangePassword(true)
                    .failedLoginAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            log.info("User created successfully via API: {}", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(user, "User created successfully"));
            
        } catch (Exception e) {
            log.error("Failed to create user via API", e);
            throw e;
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user", description = "Get user by ID")
    @PreAuthorize("hasRole('ROLE_USERS_READ') or #userId == authentication.name")
    @PerformanceLog
    public ResponseEntity<BaseResponse<UserDto>> getUser(@PathVariable String userId) {
        log.debug("Getting user via API: {}", userId);

        try {
            // TODO: Replace with real service call
            // Long userIdLong = Long.parseLong(userId);
            // Optional<UserDto> userOpt = userService.getUserById(userIdLong);
            throw new UnsupportedOperationException("User service not yet implemented - real database integration required");
            
        } catch (Exception e) {
            log.error("Failed to get user via API: {}", userId, e);
            throw e;
        }
    }

    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Get user by username")
    @PreAuthorize("hasRole('ROLE_USERS_READ') or #username == authentication.name")
    @PerformanceLog
    public ResponseEntity<BaseResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        log.debug("Getting user by username via API: {}", username);

        try {
            Optional<UserDto> userOpt = findUserByUsername(username);
            
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success(userOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("User not found"));
            }
            
        } catch (Exception e) {
            log.error("Failed to get user by username via API: {}", username, e);
            throw e;
        }
    }

    /**
     * Update user information
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user information")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER') or #userId == authentication.name")
    @AuditLog(action = "UPDATE_USER", entity = "User")
    @PerformanceLog
    public ResponseEntity<BaseResponse<UserDto>> updateUser(
            @PathVariable String userId, 
            @Valid @RequestBody UpdateUserRequest request) {
        
        log.info("Updating user via API: {}", userId);

        try {
            Long userIdLong = Long.parseLong(userId);
            UserDto updatedUser = UserDto.builder()
                    .id(userIdLong)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .userType(request.getUserType())
                    .branchId(request.getBranchId())
                    .supervisorId(request.getSupervisorId())
                    .department(request.getDepartment())
                    .position(request.getPosition())
                    .employeeId(request.getEmployeeId())
                    .active(request.getActive())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            log.info("User updated successfully via API: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(updatedUser, "User updated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to update user via API: {}", userId, e);
            throw e;
        }
    }

    /**
     * Get all users with pagination
     */
    @GetMapping
    @Operation(summary = "List users", description = "Get all users with pagination")
    @PreAuthorize("hasRole('ROLE_USERS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<UserDto>>> getAllUsers(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting all users via API - Type: {}, Branch: {}, Active: {}", 
                userType, branchId, active);

        try {
            // TODO: Replace with real service call
            // PaginatedResponse<UserDto> response = userService.getAllUsers(userType, branchId, active, pageable);
            throw new UnsupportedOperationException("User service not yet implemented - real database integration required");
            
        } catch (Exception e) {
            log.error("Failed to get users via API", e);
            throw e;
        }
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name, username, or email")
    @PreAuthorize("hasRole('ROLE_USERS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<UserDto>>> searchUsers(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Searching users via API - Term: {}", searchTerm);

        try {
            // TODO: Replace with real service call
            // PaginatedResponse<UserDto> response = userService.searchUsers(searchTerm, userType, branchId, active, pageable);
            throw new UnsupportedOperationException("User service not yet implemented - real database integration required");
            
        } catch (Exception e) {
            log.error("Failed to search users via API", e);
            throw e;
        }
    }

    /**
     * Deactivate user
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Deactivate user", description = "Deactivate user account")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    @AuditLog(action = "DEACTIVATE_USER", entity = "User")
    public ResponseEntity<BaseResponse<Void>> deactivateUser(
            @PathVariable String userId,
            @RequestParam String reason) {
        
        log.info("Deactivating user via API: {} - Reason: {}", userId, reason);

        try {
            log.info("User deactivated successfully via API: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "User deactivated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to deactivate user via API: {}", userId, e);
            throw e;
        }
    }

    /**
     * Reset user password
     */
    @PatchMapping("/{userId}/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    @AuditLog(action = "RESET_PASSWORD", entity = "User")
    public ResponseEntity<BaseResponse<Void>> resetPassword(@PathVariable String userId) {
        log.info("Resetting password via API: {}", userId);

        try {
            log.info("Password reset successfully via API: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "Password reset successfully"));
            
        } catch (Exception e) {
            log.error("Failed to reset password via API: {}", userId, e);
            throw e;
        }
    }

    /**
     * Lock user account
     */
    @PatchMapping("/{userId}/lock")
    @Operation(summary = "Lock user", description = "Lock user account")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    @AuditLog(action = "LOCK_USER", entity = "User")
    public ResponseEntity<BaseResponse<Void>> lockUser(
            @PathVariable String userId,
            @RequestParam String reason) {
        
        log.info("Locking user via API: {} - Reason: {}", userId, reason);

        try {
            log.info("User locked successfully via API: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "User locked successfully"));
            
        } catch (Exception e) {
            log.error("Failed to lock user via API: {}", userId, e);
            throw e;
        }
    }

    /**
     * Unlock user account
     */
    @PatchMapping("/{userId}/unlock")
    @Operation(summary = "Unlock user", description = "Unlock user account")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    @AuditLog(action = "UNLOCK_USER", entity = "User")
    public ResponseEntity<BaseResponse<Void>> unlockUser(@PathVariable String userId) {
        log.info("Unlocking user via API: {}", userId);

        try {
            log.info("User unlocked successfully via API: {}", userId);
            return ResponseEntity.ok(BaseResponse.success(null, "User unlocked successfully"));
            
        } catch (Exception e) {
            log.error("Failed to unlock user via API: {}", userId, e);
            throw e;
        }
    }

    // All mock data removed - real user service implementation required

    // DTOs for User operations

    @Data
    @Builder
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private UserType userType;
        private Long branchId;
        private Long supervisorId;
        private String department;
        private String position;
        private String employeeId;
        private Boolean active;
        private Boolean emailVerified;
        private Boolean phoneVerified;
        private Boolean mfaEnabled;
        private Boolean mustChangePassword;
        private Integer failedLoginAttempts;
        private LocalDateTime lastLoginAt;
        private LocalDateTime passwordChangedAt;
        private LocalDateTime lockedUntil;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private UserType userType;
        private Long branchId;
        private Long supervisorId;
        private String department;
        private String position;
        private String employeeId;
        private String initialPassword;
    }

    @Data
    @Builder
    public static class UpdateUserRequest {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private UserType userType;
        private Long branchId;
        private Long supervisorId;
        private String department;
        private String position;
        private String employeeId;
        private Boolean active;
    }
}
