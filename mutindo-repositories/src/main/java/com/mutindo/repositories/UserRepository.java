package com.mutindo.repositories;

import com.mutindo.common.enums.UserType;
import com.mutindo.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User repository - optimized for authentication and user management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Authentication queries
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE (u.username = :login OR u.email = :login) AND u.active = true")
    Optional<User> findByUsernameOrEmailAndActiveTrue(@Param("login") String login);

    // Validation queries
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);

    // Branch-specific queries
    List<User> findByBranchIdAndActiveTrue(Long branchId);
    
    Page<User> findByBranchIdAndActiveTrue(Long branchId, Pageable pageable);

    // Institution admin queries (branchId is null)
    @Query("SELECT u FROM User u WHERE u.userType = 'INSTITUTION_ADMIN' AND u.branchId IS NULL AND u.active = true")
    List<User> findInstitutionAdmins();

    // User type queries
    List<User> findByUserTypeAndActiveTrue(UserType userType);
    
    Page<User> findByUserTypeAndActiveTrue(UserType userType, Pageable pageable);

    // Search functionality
    @Query("SELECT u FROM User u WHERE u.active = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchActiveUsers(@Param("search") String search, Pageable pageable);

    // Branch-specific search
    @Query("SELECT u FROM User u WHERE u.branchId = :branchId AND u.active = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchActiveUsersByBranch(
            @Param("branchId") Long branchId,
            @Param("search") String search,
            Pageable pageable);

    // Security queries
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts AND u.active = true")
    List<User> findUsersWithExcessiveFailedAttempts(@Param("maxAttempts") Integer maxAttempts);

    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :currentTime")
    List<User> findCurrentlyLockedUsers(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT u FROM User u WHERE u.mustChangePassword = true AND u.active = true")
    List<User> findUsersRequiringPasswordChange();

    // MFA queries
    List<User> findByMfaEnabledTrueAndActiveTrue();

    // Activity tracking
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL AND u.active = true")
    List<User> findUsersNeverLoggedIn();

    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate AND u.active = true")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Department and position queries
    List<User> findByDepartmentAndActiveTrue(String department);
    
    List<User> findByPositionAndActiveTrue(String position);

    // Supervisor hierarchy - temporarily disabled due to entity field type mismatch
    // List<User> findBySupervisorIdAndActiveTrue(Long supervisorId);

    // Employee ID lookup
    Optional<User> findByEmployeeId(String employeeId);

    // Bulk operations for security
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = NULL WHERE u.id = :userId")
    void unlockUser(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    // Statistics
    @Query("SELECT COUNT(u) FROM User u WHERE u.branchId = :branchId AND u.active = true")
    long countActiveUsersByBranch(@Param("branchId") Long branchId);

    @Query("SELECT u.userType, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.userType")
    List<Object[]> getUserTypeStatistics();

    @Query("SELECT u.branchId, COUNT(u) FROM User u WHERE u.active = true AND u.branchId IS NOT NULL GROUP BY u.branchId")
    List<Object[]> getUserCountByBranch();
}
