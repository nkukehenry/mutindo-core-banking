package com.mutindo.repositories;

import com.mutindo.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Permission repository - focused only on data access
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByActive(Boolean active);

    @Query("SELECT p FROM Permission p WHERE p.active = true")
    List<Permission> findActivePermissions();

    @Query("SELECT p FROM Permission p WHERE p.active = true AND p.resource = :resource")
    List<Permission> findActivePermissionsByResource(@Param("resource") String resource);

    @Query("SELECT p FROM Permission p WHERE p.active = true AND p.action = :action")
    List<Permission> findActivePermissionsByAction(@Param("action") String action);

    @Query("SELECT p FROM Permission p WHERE p.active = true AND p.resource = :resource AND p.action = :action")
    List<Permission> findActivePermissionsByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

    @Query("SELECT p FROM Permission p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.resource) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.action) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Permission> searchActivePermissions(@Param("search") String search);

    boolean existsByName(String name);

    @Query("SELECT COUNT(p) FROM Permission p WHERE p.active = true")
    long countActivePermissions();

    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE p.active = true ORDER BY p.resource")
    List<String> findDistinctResources();
}
