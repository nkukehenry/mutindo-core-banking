package com.mutindo.repositories;

import com.mutindo.entities.ReportDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Report Definition repository - focused only on data access
 */
@Repository
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long> {

    Optional<ReportDefinition> findByReportCode(String reportCode);

    List<ReportDefinition> findByCategory(String category);

    List<ReportDefinition> findByActiveTrue();

    @Query("SELECT rd FROM ReportDefinition rd WHERE rd.active = true AND rd.category = :category")
    List<ReportDefinition> findActiveByCategory(@Param("category") String category);

    @Query("SELECT rd FROM ReportDefinition rd WHERE rd.active = true AND rd.isSystem = true")
    List<ReportDefinition> findActiveSystemReports();

    @Query("SELECT rd FROM ReportDefinition rd WHERE rd.active = true AND rd.isPublic = true")
    List<ReportDefinition> findActivePublicReports();

    @Query("SELECT rd FROM ReportDefinition rd WHERE rd.active = true AND rd.isScheduled = true")
    List<ReportDefinition> findActiveScheduledReports();

    @Query("SELECT rd FROM ReportDefinition rd WHERE rd.active = true AND rd.accessRole = :role")
    List<ReportDefinition> findActiveByAccessRole(@Param("role") String role);

    boolean existsByReportCode(String reportCode);

    @Query("SELECT COUNT(rd) FROM ReportDefinition rd WHERE rd.active = true")
    long countActiveReports();

    // Additional methods for reporting service
    Page<ReportDefinition> findByActiveTrue(Pageable pageable);
    Page<ReportDefinition> findByActiveFalse(Pageable pageable);
}
