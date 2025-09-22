package com.mutindo.repositories;

import com.mutindo.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role repository - focused only on data access
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findByActive(Boolean active);

    @Query("SELECT r FROM Role r WHERE r.active = true")
    List<Role> findActiveRoles();

    @Query("SELECT r FROM Role r WHERE r.systemRole = true")
    List<Role> findSystemRoles();

    @Query("SELECT r FROM Role r WHERE r.active = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Role> searchActiveRoles(@Param("search") String search);

    boolean existsByName(String name);

    @Query("SELECT COUNT(r) FROM Role r WHERE r.active = true")
    long countActiveRoles();
}
