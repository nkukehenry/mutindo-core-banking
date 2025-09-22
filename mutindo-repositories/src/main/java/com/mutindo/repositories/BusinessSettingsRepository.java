package com.mutindo.repositories;

import com.mutindo.entities.BusinessSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Business Settings repository - focused only on data access
 */
@Repository
public interface BusinessSettingsRepository extends JpaRepository<BusinessSettings, Long> {

    Optional<BusinessSettings> findBySettingKey(String settingKey);

    List<BusinessSettings> findBySettingType(String settingType);

    List<BusinessSettings> findByCategory(String category);

    List<BusinessSettings> findByActiveTrue();

    @Query("SELECT bs FROM BusinessSettings bs WHERE bs.active = true AND bs.settingType = :settingType")
    List<BusinessSettings> findActiveBySettingType(@Param("settingType") String settingType);

    @Query("SELECT bs FROM BusinessSettings bs WHERE bs.active = true AND bs.category = :category")
    List<BusinessSettings> findActiveByCategory(@Param("category") String category);

    @Query("SELECT bs FROM BusinessSettings bs WHERE bs.active = true AND bs.isPublic = true")
    List<BusinessSettings> findActivePublicSettings();

    boolean existsBySettingKey(String settingKey);

    @Query("SELECT COUNT(bs) FROM BusinessSettings bs WHERE bs.active = true")
    long countActiveSettings();

    // Additional methods for business settings service
    Page<BusinessSettings> findByActiveTrue(Pageable pageable);
    Page<BusinessSettings> findByActiveFalse(Pageable pageable);
}
