package com.mutindo.repositories;

import com.mutindo.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserRole repository - focused only on data access
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(String userId);

    List<UserRole> findByRoleId(String roleId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId")
    List<UserRole> findUserRoles(@Param("userId") String userId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.roleId = :roleId")
    List<UserRole> findRoleUsers(@Param("roleId") String roleId);

    @Query("SELECT ur.roleId FROM UserRole ur WHERE ur.userId = :userId")
    List<String> findRoleIdsByUserId(@Param("userId") String userId);

    @Query("SELECT ur.userId FROM UserRole ur WHERE ur.roleId = :roleId")
    List<String> findUserIdsByRoleId(@Param("roleId") String roleId);

    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.userId = :userId")
    long countUserRoles(@Param("userId") String userId);

    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.roleId = :roleId")
    long countRoleUsers(@Param("roleId") String roleId);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") String userId, @Param("roleId") String roleId);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") String roleId);

    boolean existsByUserIdAndRoleId(String userId, String roleId);
}
