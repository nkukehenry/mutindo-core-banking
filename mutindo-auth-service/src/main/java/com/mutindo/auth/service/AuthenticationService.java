package com.mutindo.auth.service;

import com.mutindo.auth.dto.*;
import com.mutindo.auth.dto.*;
import com.mutindo.auth.mapper.UserMapper;
import com.mutindo.common.context.BranchContextHolder;
import com.mutindo.common.enums.UserType;
import com.mutindo.encryption.service.IEncryptionService; // Reusing existing encryption interface
import com.mutindo.entities.User;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.jwt.dto.JwtClaims;
import com.mutindo.jwt.dto.TokenPair;
import com.mutindo.jwt.service.IJwtService; // Reusing existing JWT service interface
import com.mutindo.logging.annotation.AuditLog; // Reusing existing audit logging
import com.mutindo.logging.annotation.PerformanceLog; // Reusing existing performance logging
import com.mutindo.repositories.UserRepository; // Reusing existing repository
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Authentication service implementation
 * Reuses existing infrastructure: JWT, Encryption, Logging, Caching, Repositories
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService implements IAuthenticationService {

    // Reusing existing infrastructure components via interfaces
    private final UserRepository userRepository;
    private final IEncryptionService encryptionService;
    private final IJwtService jwtService;
    private final UserMapper userMapper;
    private final IUserRoleService userRoleService;
    private final ITokenBlacklistService tokenBlacklistService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    /**
     * Authenticate user with comprehensive security checks
     */
    @Override
    @AuditLog // Reusing existing audit logging
    @PerformanceLog // Reusing existing performance monitoring
    public LoginResponse authenticateUser(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsernameOrEmail());

        try {
            // Find user by username or email (using existing repository method)
            User user = findUserByUsernameOrEmail(request.getUsernameOrEmail());

            // Perform security validations (small methods with clear purpose)
            validateUserCanLogin(user);
            validatePassword(user, request.getPassword());

            // Update login tracking (small method)
            updateSuccessfulLogin(user);

            // Get user roles and permissions (delegated to role service)
            List<String> roles = userRoleService.getUserRoles(user.getId());
            List<String> permissions = userRoleService.getUserPermissions(user.getId());

            // Generate JWT tokens (reusing existing JWT service)
            TokenPair tokenPair = generateTokensForUser(user, roles, permissions);

            // Build user DTO
            UserDto userDto = userMapper.toDto(user);
            userDto.setRoles(roles);
            userDto.setPermissions(permissions);

            log.info("User authenticated successfully: {}", user.getUsername());
            return LoginResponse.success(
                    tokenPair.getAccessToken(),
                    tokenPair.getRefreshToken(),
                    tokenPair.getExpiresIn(),
                    userDto
            );

        } catch (Exception e) {
            log.warn("Authentication failed for user: {}", request.getUsernameOrEmail(), e);
            return LoginResponse.failure(e.getMessage());
        }
    }

    /**
     * Refresh access token using existing JWT infrastructure
     */
    @Override
    @PerformanceLog
    public LoginResponse refreshAccessToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        try {
            // Use existing JWT service to refresh token
            String newAccessToken = jwtService.refreshAccessToken(request.getRefreshToken());

            // Extract user info from refresh token
            JwtClaims claims = jwtService.validateAndExtractClaims(request.getRefreshToken());
            User user = userRepository.findById(Long.parseLong(claims.getUserId()))
                    .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

            // Validate user is still active
            if (!user.getActive()) {
                throw new BusinessException("User account is inactive", "USER_INACTIVE");
            }

            UserDto userDto = userMapper.toDto(user);
            userDto.setRoles(claims.getRoles());
            userDto.setPermissions(claims.getPermissions());

            return LoginResponse.success(
                    newAccessToken,
                    request.getRefreshToken(), // Keep same refresh token
                    15 * 60, // 15 minutes in seconds
                    userDto
            );

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BusinessException("Token refresh failed", "TOKEN_REFRESH_ERROR");
        }
    }

    /**
     * Register new user (admin operation)
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = "users", allEntries = true) // Clear user cache
    public UserDto registerUser(UserRegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Validate registration request (small method)
        validateUserRegistration(request);

        // Create user entity
        User user = createUserFromRequest(request);

        // Save user (using existing repository)
        User savedUser = userRepository.save(user);

        // Assign roles if provided (delegated to role service)
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            userRoleService.assignRolesToUser(savedUser.getId(), request.getRoleIds());
        }

        log.info("User registered successfully: {}", savedUser.getUsername());
        return userMapper.toDto(savedUser);
    }

    /**
     * Change user password with validation
     */
    @Override
    @Transactional
    @AuditLog
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);

        // Validate password change request (small method)
        validatePasswordChangeRequest(request);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        // Verify current password (reusing existing encryption service)
        if (!encryptionService.verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
        }

        // Update password (small method)
        updateUserPassword(user, request.getNewPassword());

        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Lock user account for security reasons
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = "users", key = "#userId")
    public void lockUser(Long userId, String reason) {
        log.info("Locking user account: {} - Reason: {}", userId, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        // Lock user account (small method)
        lockUserAccount(user, reason);

        log.info("User account locked: {}", userId);
    }

    /**
     * Unlock user account
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = "users", key = "#userId")
    public void unlockUser(Long userId) {
        log.info("Unlocking user account: {}", userId);

        // Use existing repository method for unlocking
        userRepository.unlockUser(userId);
        userRepository.resetFailedLoginAttempts(userId);

        log.info("User account unlocked: {}", userId);
    }

    /**
     * Logout user and invalidate tokens
     */
    @Override
    @AuditLog
    public void logoutUser(Long userId, String accessToken) {
        log.info("Logging out user: {}", userId);

        try {
            // Extract token expiration from JWT
            JwtClaims claims = jwtService.validateAndExtractClaims(accessToken);
            Long expiresAt = claims.getExpiresAt() != null ? 
                claims.getExpiresAt().getEpochSecond() : null;

            // Add token to blacklist
            tokenBlacklistService.blacklistToken(accessToken, userId, expiresAt);

            // Clear branch context
            BranchContextHolder.clearContext();

            log.info("User logged out successfully: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to logout user: {}", userId, e);
            // Still clear context even if blacklisting fails
            BranchContextHolder.clearContext();
            throw new BusinessException("Logout failed", "LOGOUT_ERROR");
        }
    }

    /**
     * Validate if user is active and valid
     */
    @Override
    @Cacheable(value = "userValidation", key = "#userId") // Cache validation results
    public boolean isUserValid(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getActive() && 
                           (user.getLockedUntil() == null || user.getLockedUntil().isBefore(LocalDateTime.now())))
                .orElse(false);
    }

    /**
     * Force logout user from all sessions (security operation)
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = "userValidation", key = "#userId")
    public void forceLogoutUser(Long userId) {
        log.info("Force logging out user from all sessions: {}", userId);

        try {
            // Blacklist all user tokens
            tokenBlacklistService.blacklistAllUserTokens(userId);

            // Clear branch context
            BranchContextHolder.clearContext();

            log.info("User force logged out successfully from all sessions: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to force logout user: {}", userId, e);
            throw new BusinessException("Force logout failed", "FORCE_LOGOUT_ERROR");
        }
    }

    // Private helper methods (small and focused)

    /**
     * Find user by username or email
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmailAndActiveTrue(usernameOrEmail)
                .orElseThrow(() -> new BusinessException("Invalid credentials", "INVALID_CREDENTIALS"));
    }

    /**
     * Validate user can login (account status, lockout, etc.)
     */
    private void validateUserCanLogin(User user) {
        if (!user.getActive()) {
            throw new BusinessException("User account is inactive", "USER_INACTIVE");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException("User account is locked", "USER_LOCKED");
        }

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            lockUserAccount(user, "Too many failed login attempts");
            throw new BusinessException("Account locked due to failed attempts", "ACCOUNT_LOCKED");
        }
    }

    /**
     * Validate password
     */
    private void validatePassword(User user, String password) {
        if (!encryptionService.verifyPassword(password, user.getPasswordHash())) {
            // Increment failed attempts
            incrementFailedLoginAttempts(user);
            throw new BusinessException("Invalid credentials", "INVALID_CREDENTIALS");
        }
    }

    /**
     * Update successful login tracking
     */
    private void updateSuccessfulLogin(User user) {
        userRepository.resetFailedLoginAttempts(user.getId());
        userRepository.updateLastLoginTime(user.getId(), LocalDateTime.now());
    }

    /**
     * Generate JWT tokens for authenticated user
     */
    private TokenPair generateTokensForUser(User user, List<String> roles, List<String> permissions) {
        JwtClaims claims = JwtClaims.builder()
                .userId(user.getId().toString())
                .branchId(user.getBranchId() != null ? user.getBranchId().toString() : null)
                .userType(user.getUserType().toString())
                .roles(roles)
                .permissions(permissions)
                .build();

        return jwtService.generateTokenPair(claims);
    }

    /**
     * Validate user registration request
     */
    private void validateUserRegistration(UserRegistrationRequest request) {
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Validate branch requirement for branch users
        if ("BRANCH_USER".equals(request.getUserType()) && request.getBranchId() == null) {
            throw new ValidationException("Branch ID is required for branch users");
        }

        // Institution admins should not have branch ID
        if ("INSTITUTION_ADMIN".equals(request.getUserType()) && request.getBranchId() != null) {
            throw new ValidationException("Institution admins should not have branch ID");
        }
    }

    /**
     * Create user entity from registration request
     */
    private User createUserFromRequest(UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(encryptionService.hashPassword(request.getPassword()));
        user.setUserType(UserType.valueOf(request.getUserType()));
        user.setBranchId(request.getBranchId() != null ? Long.parseLong(request.getBranchId()) : null);
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setSupervisorId(request.getSupervisorId() != null ? Long.parseLong(request.getSupervisorId()) : null);
        user.setMustChangePassword(request.getMustChangePassword());
        user.setActive(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setMfaEnabled(false);
        user.setFailedLoginAttempts(0);
        return user;
    }

    /**
     * Validate password change request
     */
    private void validatePasswordChangeRequest(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new ValidationException("New password must be different from current password");
        }
    }

    /**
     * Update user password
     */
    private void updateUserPassword(User user, String newPassword) {
        user.setPasswordHash(encryptionService.hashPassword(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    /**
     * Lock user account
     */
    private void lockUserAccount(User user, String reason) {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        userRepository.save(user);
    }

    /**
     * Increment failed login attempts
     */
    private void incrementFailedLoginAttempts(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        userRepository.save(user);
    }
}
