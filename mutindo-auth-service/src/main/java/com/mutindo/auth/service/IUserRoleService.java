package com.mutindo.auth.service;

import java.util.List;

/**
 * User role service interface - small and focused on role management
 */
public interface IUserRoleService {
    
    /**
     * Get user's assigned roles
     * @param userId User ID
     * @return List of role names
     */
    List<String> getUserRoles(Long userId);
    
    /**
     * Get user's effective permissions (from roles + direct assignments)
     * @param userId User ID
     * @return List of permission names
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * Assign roles to user
     * @param userId User ID
     * @param roleIds List of role IDs to assign
     */
    void assignRolesToUser(Long userId, List<String> roleIds);
    
    /**
     * Remove roles from user
     * @param userId User ID
     * @param roleIds List of role IDs to remove
     */
    void removeRolesFromUser(Long userId, List<String> roleIds);
    
    /**
     * Check if user has specific permission
     * @param userId User ID
     * @param permission Permission to check
     * @return true if user has permission
     */
    boolean hasPermission(Long userId, String permission);
}
