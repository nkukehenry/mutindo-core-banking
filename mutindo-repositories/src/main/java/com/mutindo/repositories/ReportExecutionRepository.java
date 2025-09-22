package com.mutindo.repositories;

import com.mutindo.entities.ReportExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Report Execution repository - focused only on data access
 */
@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {

    List<ReportExecution> findByReportDefinitionId(Long reportDefinitionId);

    List<ReportExecution> findByReportCode(String reportCode);

    List<ReportExecution> findByExecutedBy(String executedBy);

    List<ReportExecution> findByStatus(String status);

    @Query("SELECT re FROM ReportExecution re WHERE re.reportDefinitionId = :reportDefinitionId ORDER BY re.createdAt DESC")
    List<ReportExecution> findRecentByReportDefinition(@Param("reportDefinitionId") Long reportDefinitionId, Pageable pageable);

    @Query("SELECT re FROM ReportExecution re WHERE re.executedBy = :executedBy ORDER BY re.createdAt DESC")
    List<ReportExecution> findRecentByUser(@Param("executedBy") String executedBy, Pageable pageable);

    @Query("SELECT re FROM ReportExecution re WHERE re.status = :status AND re.createdAt >= :fromDate")
    List<ReportExecution> findFailedExecutions(@Param("status") String status, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT re FROM ReportExecution re WHERE re.isScheduled = true AND re.createdAt >= :fromDate")
    List<ReportExecution> findScheduledExecutions(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT re FROM ReportExecution re WHERE re.branchId = :branchId ORDER BY re.createdAt DESC")
    List<ReportExecution> findRecentByBranch(@Param("branchId") Long branchId, Pageable pageable);

    @Query("SELECT COUNT(re) FROM ReportExecution re WHERE re.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(re) FROM ReportExecution re WHERE re.createdAt >= :fromDate")
    long countExecutionsSince(@Param("fromDate") LocalDateTime fromDate);

    // Additional methods for reporting service
    Page<ReportExecution> findByReportDefinitionId(Long reportDefinitionId, Pageable pageable);
    Page<ReportExecution> findByExecutedBy(String executedBy, Pageable pageable);
    Page<ReportExecution> findByStatus(String status, Pageable pageable);
}
