package com.mutindo.auth.service;

import com.mutindo.entities.Role;

import java.util.List;

/**
 * User role service interface - comprehensive role and permission management
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
    
    /**
     * Grant direct permission to user (override role permissions)
     * @param userId User ID
     * @param permissionId Permission ID to grant
     */
    void grantPermissionToUser(Long userId, String permissionId);
    
    /**
     * Deny direct permission to user (override role permissions)
     * @param userId User ID
     * @param permissionId Permission ID to deny
     */
    void denyPermissionToUser(Long userId, String permissionId);
    
    /**
     * Remove direct permission from user
     * @param userId User ID
     * @param permissionId Permission ID to remove
     */
    void removePermissionFromUser(Long userId, String permissionId);
    
    /**
     * Get all available roles
     * @return List of active roles
     */
    List<Role> getAllActiveRoles();
    
    /**
     * Get all available permissions
     * @return List of active permission names
     */
    List<String> getAllActivePermissions();
}
