package com.mutindo.repositories;

import com.mutindo.entities.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserPermission repository - focused only on data access
 */
@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    List<UserPermission> findByUserId(String userId);

    List<UserPermission> findByPermissionId(String permissionId);

    List<UserPermission> findByUserIdAndEffect(String userId, UserPermission.PermissionEffect effect);

    @Query("SELECT up FROM UserPermission up WHERE up.userId = :userId")
    List<UserPermission> findUserPermissions(@Param("userId") String userId);

    @Query("SELECT up FROM UserPermission up WHERE up.permissionId = :permissionId")
    List<UserPermission> findPermissionUsers(@Param("permissionId") String permissionId);

    @Query("SELECT up.permissionId FROM UserPermission up WHERE up.userId = :userId AND up.effect = :effect")
    List<String> findPermissionIdsByUserIdAndEffect(@Param("userId") String userId, @Param("effect") UserPermission.PermissionEffect effect);

    @Query("SELECT up.userId FROM UserPermission up WHERE up.permissionId = :permissionId AND up.effect = :effect")
    List<String> findUserIdsByPermissionIdAndEffect(@Param("permissionId") String permissionId, @Param("effect") UserPermission.PermissionEffect effect);

    @Query("SELECT COUNT(up) FROM UserPermission up WHERE up.userId = :userId")
    long countUserPermissions(@Param("userId") String userId);

    @Query("SELECT COUNT(up) FROM UserPermission up WHERE up.permissionId = :permissionId")
    long countPermissionUsers(@Param("permissionId") String permissionId);

    @Modifying
    @Query("DELETE FROM UserPermission up WHERE up.userId = :userId AND up.permissionId = :permissionId")
    void deleteByUserIdAndPermissionId(@Param("userId") String userId, @Param("permissionId") String permissionId);

    @Modifying
    @Query("DELETE FROM UserPermission up WHERE up.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM UserPermission up WHERE up.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") String permissionId);

    boolean existsByUserIdAndPermissionId(String userId, String permissionId);
}
