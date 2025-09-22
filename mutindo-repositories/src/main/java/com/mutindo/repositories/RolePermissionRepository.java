package com.mutindo.repositories;

import com.mutindo.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RolePermission repository - focused only on data access
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleId(String roleId);

    List<RolePermission> findByPermissionId(String permissionId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.roleId = :roleId")
    List<RolePermission> findRolePermissions(@Param("roleId") String roleId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    List<RolePermission> findPermissionRoles(@Param("permissionId") String permissionId);

    @Query("SELECT rp.permissionId FROM RolePermission rp WHERE rp.roleId = :roleId")
    List<String> findPermissionIdsByRoleId(@Param("roleId") String roleId);

    @Query("SELECT rp.roleId FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    List<String> findRoleIdsByPermissionId(@Param("permissionId") String permissionId);

    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.roleId = :roleId")
    long countRolePermissions(@Param("roleId") String roleId);

    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    long countPermissionRoles(@Param("permissionId") String permissionId);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId = :permissionId")
    void deleteByRoleIdAndPermissionId(@Param("roleId") String roleId, @Param("permissionId") String permissionId);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") String roleId);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") String permissionId);

    boolean existsByRoleIdAndPermissionId(String roleId, String permissionId);
}
