package com.mutindo.repositories;

import com.mutindo.entities.SystemConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * System Configuration repository - focused only on data access
 */
@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {

    Optional<SystemConfiguration> findByConfigKey(String configKey);

    List<SystemConfiguration> findByCategory(String category);

    List<SystemConfiguration> findByActiveTrue();

    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.active = true AND sc.category = :category")
    List<SystemConfiguration> findActiveByCategory(@Param("category") String category);

    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.active = true AND sc.environment = :environment")
    List<SystemConfiguration> findActiveByEnvironment(@Param("environment") String environment);

    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.active = true AND sc.isSystem = true")
    List<SystemConfiguration> findActiveSystemConfigurations();

    boolean existsByConfigKey(String configKey);

    @Query("SELECT COUNT(sc) FROM SystemConfiguration sc WHERE sc.active = true")
    long countActiveConfigurations();

    // Additional methods for system configuration service
    Page<SystemConfiguration> findByActiveTrue(Pageable pageable);
    Page<SystemConfiguration> findByActiveFalse(Pageable pageable);
}
