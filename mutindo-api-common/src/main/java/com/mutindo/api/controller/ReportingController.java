package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.reporting.dto.*;
import com.mutindo.reporting.service.IReportingService;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Reporting REST API controller
 * Complete CRUD operations for report management and generation
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Report management and generation operations")
public class ReportingController {

    private final IReportingService reportingService;

    /**
     * Generate report
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate report", description = "Generate a report in the specified format")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @AuditLog(action = "GENERATE_REPORT", entity = "ReportExecution")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ReportResponse>> generateReport(@Valid @RequestBody GenerateReportRequest request) {
        log.info("Generating report via API - Code: {} - Format: {}", request.getReportCode(), request.getOutputFormat());

        try {
            ReportResponse response = reportingService.generateReport(request);
            
            return ResponseEntity.ok(BaseResponse.<ReportResponse>builder()
                    .success(true)
                    .message("Report generated successfully")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to generate report via API", e);
            throw e;
        }
    }

    /**
     * Get report execution by ID
     */
    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Get report execution", description = "Get report execution details by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ReportExecutionDto>> getReportExecution(@PathVariable String executionId) {
        log.debug("Getting report execution via API: {}", executionId);

        try {
            Long executionIdLong = Long.parseLong(executionId);
            Optional<ReportExecutionDto> executionOpt = reportingService.getReportExecution(executionIdLong);
            
            if (executionOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.<ReportExecutionDto>builder()
                        .success(true)
                        .message("Report execution retrieved successfully")
                        .data(executionOpt.get())
                        .build());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to get report execution via API: {}", executionId, e);
            throw e;
        }
    }

    /**
     * Get report executions by report definition
     */
    @GetMapping("/definitions/{reportDefinitionId}/executions")
    @Operation(summary = "Get report executions by definition", description = "Get report executions for a specific report definition")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<ReportExecutionDto>>> getReportExecutions(
            @PathVariable String reportDefinitionId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting report executions via API for definition: {}", reportDefinitionId);

        try {
            Long reportDefinitionIdLong = Long.parseLong(reportDefinitionId);
            PaginatedResponse<ReportExecutionDto> response = reportingService.getReportExecutions(reportDefinitionIdLong, pageable);
            
            return ResponseEntity.ok(BaseResponse.<PaginatedResponse<ReportExecutionDto>>builder()
                    .success(true)
                    .message("Report executions retrieved successfully")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get report executions via API: {}", reportDefinitionId, e);
            throw e;
        }
    }

    /**
     * Get user report executions
     */
    @GetMapping("/executions/user/{executedBy}")
    @Operation(summary = "Get user report executions", description = "Get report executions for a specific user")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<ReportExecutionDto>>> getUserReportExecutions(
            @PathVariable String executedBy,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting user report executions via API for user: {}", executedBy);

        try {
            PaginatedResponse<ReportExecutionDto> response = reportingService.getUserReportExecutions(executedBy, pageable);
            
            return ResponseEntity.ok(BaseResponse.<PaginatedResponse<ReportExecutionDto>>builder()
                    .success(true)
                    .message("User report executions retrieved successfully")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get user report executions via API: {}", executedBy, e);
            throw e;
        }
    }

    /**
     * Cancel report execution
     */
    @PostMapping("/executions/{executionId}/cancel")
    @Operation(summary = "Cancel report execution", description = "Cancel a running report execution")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "CANCEL_REPORT_EXECUTION", entity = "ReportExecution")
    public ResponseEntity<BaseResponse<Void>> cancelReportExecution(@PathVariable String executionId) {
        log.info("Cancelling report execution via API: {}", executionId);

        try {
            Long executionIdLong = Long.parseLong(executionId);
            reportingService.cancelReportExecution(executionIdLong);
            
            return ResponseEntity.ok(BaseResponse.<Void>builder()
                    .success(true)
                    .message("Report execution cancelled successfully")
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to cancel report execution via API: {}", executionId, e);
            throw e;
        }
    }

    /**
     * Get report definition by code
     */
    @GetMapping("/definitions/code/{reportCode}")
    @Operation(summary = "Get report definition by code", description = "Get report definition by code")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ReportDefinitionDto>> getReportDefinitionByCode(@PathVariable String reportCode) {
        log.debug("Getting report definition by code via API: {}", reportCode);

        try {
            Optional<ReportDefinitionDto> definitionOpt = reportingService.getReportDefinitionByCode(reportCode);
            
            if (definitionOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.<ReportDefinitionDto>builder()
                        .success(true)
                        .message("Report definition retrieved successfully")
                        .data(definitionOpt.get())
                        .build());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to get report definition via API: {}", reportCode, e);
            throw e;
        }
    }

    /**
     * Get report definition by ID
     */
    @GetMapping("/definitions/{reportDefinitionId}")
    @Operation(summary = "Get report definition by ID", description = "Get report definition by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ReportDefinitionDto>> getReportDefinitionById(@PathVariable String reportDefinitionId) {
        log.debug("Getting report definition by ID via API: {}", reportDefinitionId);

        try {
            Long reportDefinitionIdLong = Long.parseLong(reportDefinitionId);
            Optional<ReportDefinitionDto> definitionOpt = reportingService.getReportDefinitionById(reportDefinitionIdLong);
            
            if (definitionOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.<ReportDefinitionDto>builder()
                        .success(true)
                        .message("Report definition retrieved successfully")
                        .data(definitionOpt.get())
                        .build());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to get report definition via API: {}", reportDefinitionId, e);
            throw e;
        }
    }

    /**
     * Create report definition
     */
    @PostMapping("/definitions")
    @Operation(summary = "Create report definition", description = "Create a new report definition")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "CREATE_REPORT_DEFINITION", entity = "ReportDefinition")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ReportDefinitionDto>> createReportDefinition(@Valid @RequestBody CreateReportDefinitionRequest request) {
        log.info("Creating report definition via API - Code: {} - Category: {}", request.getReportCode(), request.getCategory());

        try {
            ReportDefinitionDto definition = reportingService.createReportDefinition(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.<ReportDefinitionDto>builder()
                            .success(true)
                            .message("Report definition created successfully")
                            .data(definition)
                            .build());
            
        } catch (Exception e) {
            log.error("Failed to create report definition via API", e);
            throw e;
        }
    }

    /**
     * Update report definition
     */
    @PutMapping("/definitions/{reportDefinitionId}")
    @Operation(summary = "Update report definition", description = "Update report definition")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "UPDATE_REPORT_DEFINITION", entity = "ReportDefinition")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ReportDefinitionDto>> updateReportDefinition(
            @PathVariable String reportDefinitionId, 
            @Valid @RequestBody UpdateReportDefinitionRequest request) {
        
        log.info("Updating report definition via API: {}", reportDefinitionId);

        try {
            Long reportDefinitionIdLong = Long.parseLong(reportDefinitionId);
            ReportDefinitionDto updatedDefinition = reportingService.updateReportDefinition(reportDefinitionIdLong, request);
            
            return ResponseEntity.ok(BaseResponse.<ReportDefinitionDto>builder()
                    .success(true)
                    .message("Report definition updated successfully")
                    .data(updatedDefinition)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to update report definition via API: {}", reportDefinitionId, e);
            throw e;
        }
    }

    /**
     * Get report definitions by category
     */
    @GetMapping("/definitions/category/{category}")
    @Operation(summary = "Get report definitions by category", description = "Get report definitions by category")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<ReportDefinitionDto>>> getReportDefinitionsByCategory(@PathVariable String category) {
        log.debug("Getting report definitions by category via API: {}", category);

        try {
            List<ReportDefinitionDto> definitions = reportingService.getReportDefinitionsByCategory(category);
            
            return ResponseEntity.ok(BaseResponse.<List<ReportDefinitionDto>>builder()
                    .success(true)
                    .message("Report definitions retrieved successfully")
                    .data(definitions)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get report definitions by category via API: {}", category, e);
            throw e;
        }
    }

    /**
     * Get public report definitions
     */
    @GetMapping("/definitions/public")
    @Operation(summary = "Get public report definitions", description = "Get public report definitions visible to all users")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<ReportDefinitionDto>>> getPublicReportDefinitions() {
        log.debug("Getting public report definitions via API");

        try {
            List<ReportDefinitionDto> definitions = reportingService.getPublicReportDefinitions();
            
            return ResponseEntity.ok(BaseResponse.<List<ReportDefinitionDto>>builder()
                    .success(true)
                    .message("Public report definitions retrieved successfully")
                    .data(definitions)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get public report definitions via API", e);
            throw e;
        }
    }

    /**
     * Get all report definitions with pagination
     */
    @GetMapping("/definitions")
    @Operation(summary = "List report definitions", description = "Get all report definitions with pagination")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<ReportDefinitionDto>>> getAllReportDefinitions(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting all report definitions via API - Active: {}", active);

        try {
            PaginatedResponse<ReportDefinitionDto> response = reportingService.getAllReportDefinitions(active, pageable);
            
            return ResponseEntity.ok(BaseResponse.<PaginatedResponse<ReportDefinitionDto>>builder()
                    .success(true)
                    .message("Report definitions retrieved successfully")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get report definitions via API", e);
            throw e;
        }
    }

    /**
     * Deactivate report definition
     */
    @DeleteMapping("/definitions/{reportDefinitionId}")
    @Operation(summary = "Deactivate report definition", description = "Deactivate report definition")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @AuditLog(action = "DEACTIVATE_REPORT_DEFINITION", entity = "ReportDefinition")
    public ResponseEntity<BaseResponse<Void>> deactivateReportDefinition(
            @PathVariable String reportDefinitionId,
            @RequestParam String reason) {
        
        log.info("Deactivating report definition via API: {} - Reason: {}", reportDefinitionId, reason);

        try {
            Long reportDefinitionIdLong = Long.parseLong(reportDefinitionId);
            reportingService.deactivateReportDefinition(reportDefinitionIdLong, reason);
            
            return ResponseEntity.ok(BaseResponse.<Void>builder()
                    .success(true)
                    .message("Report definition deactivated successfully")
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to deactivate report definition via API: {}", reportDefinitionId, e);
            throw e;
        }
    }
}
