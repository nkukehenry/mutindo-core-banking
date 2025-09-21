package com.mutindo.repositories;

import com.mutindo.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Log repository - optimized for audit trail queries and compliance reporting
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    // Entity-specific audit trails
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(
            String entityType, String entityId, Pageable pageable);

    Page<AuditLog> findByEntityTypeOrderByPerformedAtDesc(String entityType, Pageable pageable);

    // User activity tracking
    Page<AuditLog> findByPerformedByOrderByPerformedAtDesc(String performedBy, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.performedBy = :userId AND a.performedAt BETWEEN :startDate AND :endDate ORDER BY a.performedAt DESC")
    Page<AuditLog> findUserActivityByDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Action-based queries
    Page<AuditLog> findByActionOrderByPerformedAtDesc(String action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.action IN :actions ORDER BY a.performedAt DESC")
    Page<AuditLog> findByActionsOrderByPerformedAtDesc(@Param("actions") List<String> actions, Pageable pageable);

    // Branch-specific audit trails
    Page<AuditLog> findByBranchIdOrderByPerformedAtDesc(String branchId, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.branchId = :branchId AND a.performedAt BETWEEN :startDate AND :endDate ORDER BY a.performedAt DESC")
    Page<AuditLog> findBranchActivityByDateRange(
            @Param("branchId") String branchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Date range queries
    @Query("SELECT a FROM AuditLog a WHERE a.performedAt BETWEEN :startDate AND :endDate ORDER BY a.performedAt DESC")
    Page<AuditLog> findByPerformedAtBetweenOrderByPerformedAtDesc(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Correlation ID tracking (for distributed tracing)
    List<AuditLog> findByCorrelationIdOrderByPerformedAtAsc(String correlationId);

    // IP address tracking
    Page<AuditLog> findByIpAddressOrderByPerformedAtDesc(String ipAddress, Pageable pageable);

    // Search functionality
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "a.entityId LIKE CONCAT('%', :search, '%') OR " +
           "a.correlationId LIKE CONCAT('%', :search, '%')) " +
           "ORDER BY a.performedAt DESC")
    Page<AuditLog> searchAuditLogs(@Param("search") String search, Pageable pageable);

    // Security and compliance queries
    @Query("SELECT a FROM AuditLog a WHERE a.action IN ('LOGIN', 'LOGOUT', 'FAILED_LOGIN') ORDER BY a.performedAt DESC")
    Page<AuditLog> findAuthenticationEvents(Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.action IN ('CREATE', 'UPDATE', 'DELETE') AND a.entityType IN :sensitiveEntities ORDER BY a.performedAt DESC")
    Page<AuditLog> findSensitiveDataChanges(@Param("sensitiveEntities") List<String> sensitiveEntities, Pageable pageable);

    // Statistics and analytics
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.performedAt BETWEEN :startDate AND :endDate GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> getActionStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a.performedBy, COUNT(a) FROM AuditLog a WHERE a.performedAt BETWEEN :startDate AND :endDate GROUP BY a.performedBy ORDER BY COUNT(a) DESC")
    List<Object[]> getUserActivityStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a WHERE a.performedAt BETWEEN :startDate AND :endDate GROUP BY a.entityType ORDER BY COUNT(a) DESC")
    List<Object[]> getEntityTypeStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a.branchId, COUNT(a) FROM AuditLog a WHERE a.branchId IS NOT NULL AND a.performedAt BETWEEN :startDate AND :endDate GROUP BY a.branchId ORDER BY COUNT(a) DESC")
    List<Object[]> getBranchActivityStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Compliance reporting
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.performedAt BETWEEN :startDate AND :endDate")
    long countAuditLogsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DATE(a.performedAt), COUNT(a) FROM AuditLog a WHERE a.performedAt BETWEEN :startDate AND :endDate GROUP BY DATE(a.performedAt) ORDER BY DATE(a.performedAt)")
    List<Object[]> getDailyAuditLogCounts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Data retention queries
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.performedAt < :cutoffDate")
    long countAuditLogsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Suspicious activity detection
    @Query("SELECT a.performedBy, COUNT(a) FROM AuditLog a WHERE a.performedAt > :recentTime GROUP BY a.performedBy HAVING COUNT(a) > :threshold ORDER BY COUNT(a) DESC")
    List<Object[]> findHighActivityUsers(
            @Param("recentTime") LocalDateTime recentTime,
            @Param("threshold") Long threshold);

    @Query("SELECT a.ipAddress, COUNT(DISTINCT a.performedBy) FROM AuditLog a WHERE a.performedAt > :recentTime GROUP BY a.ipAddress HAVING COUNT(DISTINCT a.performedBy) > :threshold")
    List<Object[]> findSuspiciousIPAddresses(
            @Param("recentTime") LocalDateTime recentTime,
            @Param("threshold") Long threshold);
}
