package com.mutindo.auth.service;

import com.mutindo.entities.Role;
import com.mutindo.entities.UserPermission;
import com.mutindo.entities.UserRole;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.PermissionRepository;
import com.mutindo.repositories.RolePermissionRepository;
import com.mutindo.repositories.RoleRepository;
import com.mutindo.repositories.UserPermissionRepository;
import com.mutindo.repositories.UserRepository;
import com.mutindo.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User role service implementation - complete role-permission system
 * Reuses existing repository infrastructure and caching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService implements IUserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    /**
     * Get user's roles with caching for performance
     */
    @Override
    @Cacheable(value = "userRoles", key = "#userId")
    @PerformanceLog
    public List<String> getUserRoles(Long userId) {
        log.debug("Getting roles for user: {}", userId);
        
        List<String> roleIds = userRoleRepository.findRoleIdsByUserId(userId.toString());
        List<Role> roles = roleRepository.findAllById(
            roleIds.stream().map(Long::parseLong).collect(Collectors.toList())
        );
        
        return roles.stream()
                .filter(Role::getActive)
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get user's effective permissions with caching
     * Combines role permissions + direct user permissions (GRANT/DENY)
     */
    @Override
    @Cacheable(value = "userPermissions", key = "#userId")
    @PerformanceLog
    public List<String> getUserPermissions(Long userId) {
        log.debug("Getting permissions for user: {}", userId);
        
        Set<String> permissions = new HashSet<>();
        
        // Get role-based permissions
        List<String> roleIds = userRoleRepository.findRoleIdsByUserId(userId.toString());
        for (String roleId : roleIds) {
            List<String> rolePermissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
            List<String> rolePermissions = permissionRepository.findAllById(
                rolePermissionIds.stream().map(Long::parseLong).collect(Collectors.toList())
            ).stream()
            .filter(permission -> permission.getActive())
            .map(permission -> permission.getName())
            .collect(Collectors.toList());
            
            permissions.addAll(rolePermissions);
        }
        
        // Apply user-level permission overrides (GRANT/DENY)
        List<UserPermission> userPermissions = userPermissionRepository.findUserPermissions(userId.toString());
        for (UserPermission userPermission : userPermissions) {
            String permissionName = permissionRepository.findById(Long.parseLong(userPermission.getPermissionId()))
                    .map(permission -> permission.getName())
                    .orElse(null);
            
            if (permissionName != null) {
                if (userPermission.getEffect() == UserPermission.PermissionEffect.GRANT) {
                    permissions.add(permissionName);
                } else if (userPermission.getEffect() == UserPermission.PermissionEffect.DENY) {
                    permissions.remove(permissionName);
                }
            }
        }
        
        return new ArrayList<>(permissions);
    }

    /**
     * Assign roles to user
     */
    @Override
    @Transactional
    @CacheEvict(value = {"userRoles", "userPermissions", "userPermissionCheck"}, allEntries = true)
    public void assignRolesToUser(Long userId, List<String> roleIds) {
        log.info("Assigning roles to user: {} - Roles: {}", userId, roleIds);
        
        for (String roleId : roleIds) {
            if (!userRoleRepository.existsByUserIdAndRoleId(userId.toString(), roleId)) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId.toString());
                userRole.setRoleId(roleId);
                userRole.setAssignedAt(LocalDateTime.now());
                // TODO: Set assignedBy from current user context
                
                userRoleRepository.save(userRole);
            }
        }
    }

    /**
     * Remove roles from user
     */
    @Override
    @Transactional
    @CacheEvict(value = {"userRoles", "userPermissions", "userPermissionCheck"}, allEntries = true)
    public void removeRolesFromUser(Long userId, List<String> roleIds) {
        log.info("Removing roles from user: {} - Roles: {}", userId, roleIds);
        
        for (String roleId : roleIds) {
            userRoleRepository.deleteByUserIdAndRoleId(userId.toString(), roleId);
        }
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

    /**
     * Grant direct permission to user (override role permissions)
     */
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck"}, allEntries = true)
    public void grantPermissionToUser(Long userId, String permissionId) {
        log.info("Granting permission to user: {} - Permission: {}", userId, permissionId);
        
        if (!userPermissionRepository.existsByUserIdAndPermissionId(userId.toString(), permissionId)) {
            UserPermission userPermission = new UserPermission();
            userPermission.setUserId(userId.toString());
            userPermission.setPermissionId(permissionId);
            userPermission.setEffect(UserPermission.PermissionEffect.GRANT);
            
            userPermissionRepository.save(userPermission);
        }
    }

    /**
     * Deny direct permission to user (override role permissions)
     */
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck"}, allEntries = true)
    public void denyPermissionToUser(Long userId, String permissionId) {
        log.info("Denying permission to user: {} - Permission: {}", userId, permissionId);
        
        if (!userPermissionRepository.existsByUserIdAndPermissionId(userId.toString(), permissionId)) {
            UserPermission userPermission = new UserPermission();
            userPermission.setUserId(userId.toString());
            userPermission.setPermissionId(permissionId);
            userPermission.setEffect(UserPermission.PermissionEffect.DENY);
            
            userPermissionRepository.save(userPermission);
        }
    }

    /**
     * Remove direct permission from user
     */
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck"}, allEntries = true)
    public void removePermissionFromUser(Long userId, String permissionId) {
        log.info("Removing permission from user: {} - Permission: {}", userId, permissionId);
        
        userPermissionRepository.deleteByUserIdAndPermissionId(userId.toString(), permissionId);
    }

    /**
     * Get all available roles
     */
    @Cacheable(value = "allRoles")
    public List<Role> getAllActiveRoles() {
        return roleRepository.findActiveRoles();
    }

    /**
     * Get all available permissions
     */
    @Cacheable(value = "allPermissions")
    public List<String> getAllActivePermissions() {
        return permissionRepository.findActivePermissions()
                .stream()
                .map(permission -> permission.getName())
                .collect(Collectors.toList());
    }
}
