package com.mutindo.repositories;

import com.mutindo.entities.Biometric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Biometric repository - optimized for biometric template management
 */
@Repository
public interface BiometricRepository extends JpaRepository<Biometric, String> {

    // Customer biometric queries
    List<Biometric> findByCustomerIdAndActiveTrue(String customerId);
    
    Optional<Biometric> findFirstByCustomerIdAndActiveTrueOrderByCreatedAtDesc(String customerId);

    // Biometric type queries
    List<Biometric> findByBiometricTypeAndActiveTrue(String biometricType);
    
    List<Biometric> findByCustomerIdAndBiometricTypeAndActiveTrue(String customerId, String biometricType);

    // Format-based queries
    List<Biometric> findByFormatAndActiveTrue(String format);

    // Quality-based queries
    @Query("SELECT b FROM Biometric b WHERE b.qualityScore >= :minScore AND b.active = true")
    List<Biometric> findByMinimumQualityScore(@Param("minScore") Integer minScore);

    // Device tracking
    List<Biometric> findByCaptureDeviceAndActiveTrue(String captureDevice);

    // Capture date queries
    @Query("SELECT b FROM Biometric b WHERE b.capturedAt BETWEEN :startDate AND :endDate AND b.active = true")
    List<Biometric> findByCapturedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Operator tracking
    List<Biometric> findByCapturedByAndActiveTrue(String capturedBy);

    // Consent tracking
    List<Biometric> findByConsentRefAndActiveTrue(String consentRef);
    
    @Query("SELECT b FROM Biometric b WHERE b.consentRef IS NULL AND b.active = true")
    List<Biometric> findBiometricsWithoutConsent();

    // Statistics
    @Query("SELECT b.biometricType, COUNT(b), AVG(b.qualityScore) FROM Biometric b WHERE b.active = true GROUP BY b.biometricType")
    List<Object[]> getBiometricStatistics();

    @Query("SELECT b.captureDevice, COUNT(b) FROM Biometric b WHERE b.active = true GROUP BY b.captureDevice")
    List<Object[]> getDeviceUsageStatistics();

    // Validation
    @Query("SELECT COUNT(b) FROM Biometric b WHERE b.customerId = :customerId AND b.biometricType = :type AND b.active = true")
    long countActiveByCustomerAndType(@Param("customerId") String customerId, @Param("type") String type);

    boolean existsByCustomerIdAndBiometricTypeAndActiveTrue(String customerId, String biometricType);
}
