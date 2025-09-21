package com.mutindo.auth.service;

import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User role service implementation - small and focused
 * Reuses existing repository infrastructure and caching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService implements IUserRoleService {

    private final UserRepository userRepository;
    // TODO: Inject RoleRepository, PermissionRepository when created

    /**
     * Get user's roles with caching for performance
     */
    @Override
    @Cacheable(value = "userRoles", key = "#userId")
    @PerformanceLog
    public List<String> getUserRoles(Long userId) {
        log.debug("Getting roles for user: {}", userId);
        
        // TODO: Implement when role repositories are created
        // This is a placeholder implementation
        return List.of("USER"); // Default role
    }

    /**
     * Get user's effective permissions with caching
     */
    @Override
    @Cacheable(value = "userPermissions", key = "#userId")
    @PerformanceLog
    public List<String> getUserPermissions(Long userId) {
        log.debug("Getting permissions for user: {}", userId);
        
        // TODO: Implement when permission repositories are created
        // This would combine role permissions + direct user permissions
        return List.of("accounts:read", "transactions:read"); // Default permissions
    }

    /**
     * Assign roles to user
     */
    @Override
    public void assignRolesToUser(Long userId, List<String> roleIds) {
        log.info("Assigning roles to user: {} - Roles: {}", userId, roleIds);
        
        // TODO: Implement when UserRoleRepository is available
        // This would create UserRole entities for each role assignment
    }

    /**
     * Remove roles from user
     */
    @Override
    public void removeRolesFromUser(Long userId, List<String> roleIds) {
        log.info("Removing roles from user: {} - Roles: {}", userId, roleIds);
        
        // TODO: Implement when UserRoleRepository is available
    }

    /**
     * Check if user has specific permission
     */
    @Override
    @Cacheable(value = "userPermissionCheck", key = "#userId + ':' + #permission")
    public boolean hasPermission(Long userId, String permission) {
        log.debug("Checking permission for user: {} - Permission: {}", userId, permission);
        
        List<String> userPermissions = getUserPermissions(userId);
        return userPermissions.contains(permission);
    }
}
