package com.mutindo.reporting.service;

import com.mutindo.reporting.dto.ReportDefinitionDto;
import com.mutindo.reporting.dto.ReportExecutionDto;
import com.mutindo.reporting.dto.ReportResponse;
import com.mutindo.reporting.dto.GenerateReportRequest;
import com.mutindo.reporting.dto.CreateReportDefinitionRequest;
import com.mutindo.reporting.dto.UpdateReportDefinitionRequest;
import com.mutindo.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Reporting service interface for comprehensive report management
 * Follows our established pattern of interface-driven design
 */
public interface IReportingService {
    
    /**
     * Generate report based on request
     * @param request Report generation request
     * @return Report generation response
     */
    ReportResponse generateReport(GenerateReportRequest request);
    
    /**
     * Get report execution by ID
     * @param executionId Execution ID
     * @return Report execution details
     */
    Optional<ReportExecutionDto> getReportExecution(Long executionId);
    
    /**
     * Get report executions by report definition
     * @param reportDefinitionId Report definition ID
     * @param pageable Pagination parameters
     * @return Paginated list of executions
     */
    PaginatedResponse<ReportExecutionDto> getReportExecutions(Long reportDefinitionId, Pageable pageable);
    
    /**
     * Get report executions by user
     * @param executedBy User ID
     * @param pageable Pagination parameters
     * @return Paginated list of executions
     */
    PaginatedResponse<ReportExecutionDto> getUserReportExecutions(String executedBy, Pageable pageable);
    
    /**
     * Cancel running report execution
     * @param executionId Execution ID
     */
    void cancelReportExecution(Long executionId);
    
    /**
     * Get report definition by code
     * @param reportCode Report code
     * @return Report definition if found
     */
    Optional<ReportDefinitionDto> getReportDefinitionByCode(String reportCode);
    
    /**
     * Get report definition by ID
     * @param reportDefinitionId Report definition ID
     * @return Report definition if found
     */
    Optional<ReportDefinitionDto> getReportDefinitionById(Long reportDefinitionId);
    
    /**
     * Create new report definition
     * @param request Report definition creation request
     * @return Created report definition
     */
    ReportDefinitionDto createReportDefinition(CreateReportDefinitionRequest request);
    
    /**
     * Update report definition
     * @param reportDefinitionId Report definition ID
     * @param request Update request
     * @return Updated report definition
     */
    ReportDefinitionDto updateReportDefinition(Long reportDefinitionId, UpdateReportDefinitionRequest request);
    
    /**
     * Deactivate report definition
     * @param reportDefinitionId Report definition ID
     * @param reason Deactivation reason
     */
    void deactivateReportDefinition(Long reportDefinitionId, String reason);
    
    /**
     * Get report definitions by category
     * @param category Report category
     * @return List of report definitions
     */
    List<ReportDefinitionDto> getReportDefinitionsByCategory(String category);
    
    /**
     * Get all active report definitions
     * @return List of active report definitions
     */
    List<ReportDefinitionDto> getActiveReportDefinitions();
    
    /**
     * Get public report definitions
     * @return List of public report definitions
     */
    List<ReportDefinitionDto> getPublicReportDefinitions();
    
    /**
     * Get all report definitions with pagination
     * @param active Filter by active status (null for all)
     * @param pageable Pagination parameters
     * @return Paginated list of report definitions
     */
    PaginatedResponse<ReportDefinitionDto> getAllReportDefinitions(Boolean active, Pageable pageable);
    
    /**
     * Check if report definition exists by code
     * @param reportCode Report code
     * @return true if report definition exists
     */
    boolean reportDefinitionExistsByCode(String reportCode);
    
    /**
     * Check if report definition exists by ID
     * @param reportDefinitionId Report definition ID
     * @return true if report definition exists
     */
    boolean reportDefinitionExistsById(Long reportDefinitionId);
}
