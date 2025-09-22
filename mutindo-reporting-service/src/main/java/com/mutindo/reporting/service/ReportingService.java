package com.mutindo.reporting.service;

import com.mutindo.reporting.dto.*;
import com.mutindo.reporting.mapper.ReportDefinitionMapper;
import com.mutindo.reporting.mapper.ReportExecutionMapper;
import com.mutindo.reporting.export.IExportService;
import com.mutindo.reporting.export.PdfExportService;
import com.mutindo.reporting.export.ExcelExportService;
import com.mutindo.reporting.export.CsvExportService;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.entities.ReportDefinition;
import com.mutindo.entities.ReportExecution;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.ReportDefinitionRepository;
import com.mutindo.repositories.ReportExecutionRepository;
import com.mutindo.email.service.IEmailService;
import com.mutindo.email.dto.EmailRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Comprehensive Reporting Service implementation
 * Reuses existing infrastructure: Logging, Caching, Repositories, Validation, Email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService implements IReportingService {

    // Reusing existing infrastructure components via interfaces
    private final ReportDefinitionRepository reportDefinitionRepository;
    private final ReportExecutionRepository reportExecutionRepository;
    private final ReportDefinitionMapper reportDefinitionMapper;
    private final ReportExecutionMapper reportExecutionMapper;
    private final IEmailService emailService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    // Export services
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;
    private final CsvExportService csvExportService;

    /**
     * Generate report based on request
     */
    @Override
    @Transactional
    @AuditLog(action = "GENERATE_REPORT", entity = "ReportExecution")
    @PerformanceLog
    public ReportResponse generateReport(GenerateReportRequest request) {
        log.info("Generating report: {} - Format: {}", request.getReportCode(), request.getOutputFormat());

        LocalDateTime startTime = LocalDateTime.now();
        ReportExecution execution = null;

        try {
            // Find report definition
            ReportDefinition reportDefinition = reportDefinitionRepository.findByReportCode(request.getReportCode())
                    .orElseThrow(() -> new BusinessException("Report definition not found", "REPORT_NOT_FOUND"));

            if (!reportDefinition.getActive()) {
                throw new BusinessException("Report definition is inactive", "REPORT_INACTIVE");
            }

            // Create execution record
            execution = createExecutionRecord(reportDefinition, request, startTime);

            // Execute SQL query
            List<Map<String, Object>> data = executeQuery(reportDefinition.getSqlQuery(), request.getParameters());

            // Export to requested format
            ReportResponse response = exportReport(data, reportDefinition, request);

            // Update execution record
            updateExecutionRecord(execution, response, data.size());

            // Send email if requested
            if (request.getSendEmail() != null && request.getSendEmail() && request.getEmailRecipients() != null) {
                sendReportEmail(response, request.getEmailRecipients(), request.getEmailSubject(), request.getEmailMessage());
                execution.setEmailSent(true);
                execution.setEmailRecipients(convertListToJson(request.getEmailRecipients()));
                reportExecutionRepository.save(execution);
            }

            log.info("Report generated successfully: {} - Execution ID: {}", request.getReportCode(), execution.getId());
            return response;

        } catch (Exception e) {
            log.error("Failed to generate report: {}", request.getReportCode(), e);
            
            if (execution != null) {
                execution.setStatus("FAILED");
                execution.setErrorMessage(e.getMessage());
                execution.setExecutionEndTime(LocalDateTime.now());
                reportExecutionRepository.save(execution);
            }

            throw new BusinessException("Report generation failed: " + e.getMessage(), "REPORT_GENERATION_FAILED");
        }
    }

    /**
     * Get report execution by ID
     */
    @Override
    @Cacheable(value = "reportExecutions", key = "#executionId")
    @PerformanceLog
    public Optional<ReportExecutionDto> getReportExecution(Long executionId) {
        log.debug("Getting report execution: {}", executionId);

        return reportExecutionRepository.findById(executionId)
                .map(reportExecutionMapper::toDto);
    }

    /**
     * Get report executions by report definition
     */
    @Override
    @PerformanceLog
    public PaginatedResponse<ReportExecutionDto> getReportExecutions(Long reportDefinitionId, Pageable pageable) {
        log.debug("Getting report executions for definition: {}", reportDefinitionId);

        Page<ReportExecution> executionsPage = reportExecutionRepository.findByReportDefinitionId(reportDefinitionId, pageable);
        List<ReportExecutionDto> executions = executionsPage.getContent()
                .stream()
                .map(reportExecutionMapper::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                executions,
                executionsPage.getNumber(),
                executionsPage.getSize(),
                executionsPage.getTotalElements()
        );
    }

    /**
     * Get report executions by user
     */
    @Override
    @PerformanceLog
    public PaginatedResponse<ReportExecutionDto> getUserReportExecutions(String executedBy, Pageable pageable) {
        log.debug("Getting report executions for user: {}", executedBy);

        Page<ReportExecution> executionsPage = reportExecutionRepository.findByExecutedBy(executedBy, pageable);
        List<ReportExecutionDto> executions = executionsPage.getContent()
                .stream()
                .map(reportExecutionMapper::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                executions,
                executionsPage.getNumber(),
                executionsPage.getSize(),
                executionsPage.getTotalElements()
        );
    }

    /**
     * Cancel running report execution
     */
    @Override
    @Transactional
    @AuditLog(action = "CANCEL_REPORT_EXECUTION", entity = "ReportExecution")
    public void cancelReportExecution(Long executionId) {
        log.info("Cancelling report execution: {}", executionId);

        ReportExecution execution = reportExecutionRepository.findById(executionId)
                .orElseThrow(() -> new BusinessException("Report execution not found", "EXECUTION_NOT_FOUND"));

        if (!"RUNNING".equals(execution.getStatus()) && !"PENDING".equals(execution.getStatus())) {
            throw new BusinessException("Cannot cancel execution with status: " + execution.getStatus(), "INVALID_EXECUTION_STATUS");
        }

        execution.setStatus("CANCELLED");
        execution.setExecutionEndTime(LocalDateTime.now());
        reportExecutionRepository.save(execution);

        log.info("Report execution cancelled: {}", executionId);
    }

    /**
     * Get report definition by code
     */
    @Override
    @Cacheable(value = "reportDefinitions", key = "#reportCode")
    @PerformanceLog
    public Optional<ReportDefinitionDto> getReportDefinitionByCode(String reportCode) {
        log.debug("Getting report definition by code: {}", reportCode);

        return reportDefinitionRepository.findByReportCode(reportCode)
                .map(reportDefinitionMapper::toDto);
    }

    /**
     * Get report definition by ID
     */
    @Override
    @Cacheable(value = "reportDefinitions", key = "#reportDefinitionId")
    @PerformanceLog
    public Optional<ReportDefinitionDto> getReportDefinitionById(Long reportDefinitionId) {
        log.debug("Getting report definition by ID: {}", reportDefinitionId);

        return reportDefinitionRepository.findById(reportDefinitionId)
                .map(reportDefinitionMapper::toDto);
    }

    /**
     * Create new report definition
     */
    @Override
    @Transactional
    @AuditLog(action = "CREATE_REPORT_DEFINITION", entity = "ReportDefinition")
    @CacheEvict(value = {"reportDefinitions", "publicReports"}, allEntries = true)
    public ReportDefinitionDto createReportDefinition(CreateReportDefinitionRequest request) {
        log.info("Creating report definition: {} - Category: {}", request.getReportCode(), request.getCategory());

        // Validate request
        validateCreateReportDefinitionRequest(request);

        // Check for duplicate code
        if (reportDefinitionRepository.existsByReportCode(request.getReportCode())) {
            throw new ValidationException("Report code already exists: " + request.getReportCode());
        }

        // Create report definition
        ReportDefinition reportDefinition = reportDefinitionMapper.toEntity(request);
        reportDefinition.setActive(true);

        // Set defaults
        if (reportDefinition.getIsSystem() == null) {
            reportDefinition.setIsSystem(false);
        }
        if (reportDefinition.getIsScheduled() == null) {
            reportDefinition.setIsScheduled(false);
        }
        if (reportDefinition.getIsPublic() == null) {
            reportDefinition.setIsPublic(false);
        }
        if (reportDefinition.getCacheDuration() == null) {
            reportDefinition.setCacheDuration(0);
        }
        if (reportDefinition.getMaxRows() == null) {
            reportDefinition.setMaxRows(10000);
        }

        ReportDefinition savedDefinition = reportDefinitionRepository.save(reportDefinition);

        log.info("Report definition created successfully: {} - ID: {}", savedDefinition.getReportCode(), savedDefinition.getId());
        return reportDefinitionMapper.toDto(savedDefinition);
    }

    /**
     * Update report definition
     */
    @Override
    @Transactional
    @AuditLog(action = "UPDATE_REPORT_DEFINITION", entity = "ReportDefinition")
    @CacheEvict(value = {"reportDefinitions", "publicReports"}, allEntries = true)
    public ReportDefinitionDto updateReportDefinition(Long reportDefinitionId, UpdateReportDefinitionRequest request) {
        log.info("Updating report definition: {}", reportDefinitionId);

        ReportDefinition existingDefinition = reportDefinitionRepository.findById(reportDefinitionId)
                .orElseThrow(() -> new BusinessException("Report definition not found", "REPORT_DEFINITION_NOT_FOUND"));

        // Check if definition is system definition
        if (existingDefinition.getIsSystem()) {
            throw new BusinessException("Cannot update system report definitions", "SYSTEM_REPORT_READONLY");
        }

        // Update definition
        reportDefinitionMapper.updateEntity(existingDefinition, request);
        ReportDefinition savedDefinition = reportDefinitionRepository.save(existingDefinition);

        log.info("Report definition updated successfully: {} - ID: {}", savedDefinition.getReportCode(), savedDefinition.getId());
        return reportDefinitionMapper.toDto(savedDefinition);
    }

    /**
     * Deactivate report definition
     */
    @Override
    @Transactional
    @AuditLog(action = "DEACTIVATE_REPORT_DEFINITION", entity = "ReportDefinition")
    @CacheEvict(value = {"reportDefinitions", "publicReports"}, allEntries = true)
    public void deactivateReportDefinition(Long reportDefinitionId, String reason) {
        log.info("Deactivating report definition: {} - Reason: {}", reportDefinitionId, reason);

        ReportDefinition reportDefinition = reportDefinitionRepository.findById(reportDefinitionId)
                .orElseThrow(() -> new BusinessException("Report definition not found", "REPORT_DEFINITION_NOT_FOUND"));

        if (reportDefinition.getIsSystem()) {
            throw new BusinessException("Cannot deactivate system report definitions", "SYSTEM_REPORT_READONLY");
        }

        reportDefinition.setActive(false);
        reportDefinitionRepository.save(reportDefinition);

        log.info("Report definition deactivated successfully: {} - ID: {}", reportDefinition.getReportCode(), reportDefinition.getId());
    }

    /**
     * Get report definitions by category
     */
    @Override
    @Cacheable(value = "reportDefinitions", key = "'category:' + #category")
    @PerformanceLog
    public List<ReportDefinitionDto> getReportDefinitionsByCategory(String category) {
        log.debug("Getting report definitions by category: {}", category);

        return reportDefinitionRepository.findActiveByCategory(category)
                .stream()
                .map(reportDefinitionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active report definitions
     */
    @Override
    @Cacheable(value = "reportDefinitions", key = "'active'")
    @PerformanceLog
    public List<ReportDefinitionDto> getActiveReportDefinitions() {
        log.debug("Getting all active report definitions");

        return reportDefinitionRepository.findByActiveTrue()
                .stream()
                .map(reportDefinitionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get public report definitions
     */
    @Override
    @Cacheable(value = "publicReports")
    @PerformanceLog
    public List<ReportDefinitionDto> getPublicReportDefinitions() {
        log.debug("Getting public report definitions");

        return reportDefinitionRepository.findActivePublicReports()
                .stream()
                .map(reportDefinitionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all report definitions with pagination
     */
    @Override
    @PerformanceLog
    public PaginatedResponse<ReportDefinitionDto> getAllReportDefinitions(Boolean active, Pageable pageable) {
        log.debug("Getting all report definitions - Active: {}", active);

        Page<ReportDefinition> definitionsPage;
        if (active != null) {
            if (active) {
                definitionsPage = reportDefinitionRepository.findByActiveTrue(pageable);
            } else {
                definitionsPage = reportDefinitionRepository.findByActiveFalse(pageable);
            }
        } else {
            definitionsPage = reportDefinitionRepository.findAll(pageable);
        }

        List<ReportDefinitionDto> definitions = definitionsPage.getContent()
                .stream()
                .map(reportDefinitionMapper::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                definitions,
                definitionsPage.getNumber(),
                definitionsPage.getSize(),
                definitionsPage.getTotalElements()
        );
    }

    /**
     * Check if report definition exists by code
     */
    @Override
    @PerformanceLog
    public boolean reportDefinitionExistsByCode(String reportCode) {
        return reportDefinitionRepository.existsByReportCode(reportCode);
    }

    /**
     * Check if report definition exists by ID
     */
    @Override
    @PerformanceLog
    public boolean reportDefinitionExistsById(Long reportDefinitionId) {
        return reportDefinitionRepository.existsById(reportDefinitionId);
    }

    // Private helper methods

    /**
     * Create execution record
     */
    private ReportExecution createExecutionRecord(ReportDefinition reportDefinition, GenerateReportRequest request, LocalDateTime startTime) {
        ReportExecution execution = new ReportExecution();
        execution.setReportDefinitionId(reportDefinition.getId());
        execution.setReportCode(reportDefinition.getReportCode());
        execution.setStatus("RUNNING");
        execution.setOutputFormat(request.getOutputFormat() != null ? request.getOutputFormat() : "PDF");
        execution.setParameters(convertMapToJson(request.getParameters()));
        execution.setExecutedBy("SYSTEM"); // TODO: Get from security context
        execution.setExecutionStartTime(startTime);
        execution.setIsScheduled(request.getIsScheduled() != null ? request.getIsScheduled() : false);
        execution.setBranchId(request.getBranchId());

        return reportExecutionRepository.save(execution);
    }

    /**
     * Execute SQL query
     */
    private List<Map<String, Object>> executeQuery(String sqlQuery, Map<String, Object> parameters) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            throw new ValidationException("SQL query is required");
        }

        try {
            // Replace parameters in SQL query
            String processedQuery = processQueryParameters(sqlQuery, parameters);
            
            // Execute query
            return jdbcTemplate.queryForList(processedQuery);
        } catch (Exception e) {
            log.error("Failed to execute SQL query", e);
            throw new BusinessException("Query execution failed: " + e.getMessage(), "QUERY_EXECUTION_FAILED");
        }
    }

    /**
     * Process query parameters
     */
    private String processQueryParameters(String sqlQuery, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return sqlQuery;
        }

        String processedQuery = sqlQuery;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = ":" + entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processedQuery = processedQuery.replace(placeholder, "'" + value + "'");
        }

        return processedQuery;
    }

    /**
     * Export report to requested format
     */
    private ReportResponse exportReport(List<Map<String, Object>> data, ReportDefinition reportDefinition, GenerateReportRequest request) {
        String outputFormat = request.getOutputFormat() != null ? request.getOutputFormat().toUpperCase() : "PDF";
        IExportService exportService;

        switch (outputFormat) {
            case "PDF":
                exportService = pdfExportService;
                break;
            case "EXCEL":
                exportService = excelExportService;
                break;
            case "CSV":
                exportService = csvExportService;
                break;
            default:
                throw new ValidationException("Unsupported output format: " + outputFormat);
        }

        switch (outputFormat) {
            case "PDF":
                return exportService.exportToPdf(data, reportDefinition.getReportName(), request.getParameters());
            case "EXCEL":
                return exportService.exportToExcel(data, reportDefinition.getReportName(), request.getParameters());
            case "CSV":
                return exportService.exportToCsv(data, reportDefinition.getReportName(), request.getParameters());
            default:
                throw new ValidationException("Unsupported output format: " + outputFormat);
        }
    }

    /**
     * Update execution record
     */
    private void updateExecutionRecord(ReportExecution execution, ReportResponse response, int rowsReturned) {
        execution.setStatus(response.getStatus());
        execution.setExecutionEndTime(response.getExecutionEndTime());
        execution.setExecutionDuration(response.getExecutionDuration());
        execution.setRowsReturned(rowsReturned);
        execution.setFilePath(response.getFilePath());
        execution.setFileSize(response.getFileSize());

        reportExecutionRepository.save(execution);
    }

    /**
     * Send report via email
     */
    private void sendReportEmail(ReportResponse response, List<String> recipients, String subject, String message) {
        try {
            String emailSubject = subject != null ? subject : "Report: " + response.getReportName();
            String emailMessage = message != null ? message : "Please find the attached report.";

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(String.join(",", recipients))
                    .subject(emailSubject)
                    .body(emailMessage)
                    .build();

            emailService.sendSimpleEmail(emailRequest);
            log.info("Report email sent successfully to: {}", recipients);

        } catch (Exception e) {
            log.error("Failed to send report email", e);
            throw new BusinessException("Email delivery failed: " + e.getMessage(), "EMAIL_DELIVERY_FAILED");
        }
    }

    /**
     * Validate create report definition request
     */
    private void validateCreateReportDefinitionRequest(CreateReportDefinitionRequest request) {
        if (request == null) {
            throw new ValidationException("Report definition request cannot be null");
        }

        if (request.getReportCode() == null || request.getReportCode().trim().isEmpty()) {
            throw new ValidationException("Report code is required");
        }

        if (request.getReportName() == null || request.getReportName().trim().isEmpty()) {
            throw new ValidationException("Report name is required");
        }

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new ValidationException("Report category is required");
        }

        if (request.getReportType() == null || request.getReportType().trim().isEmpty()) {
            throw new ValidationException("Report type is required");
        }

        // Validate code format
        if (!request.getReportCode().matches("^[A-Z0-9_]+$")) {
            throw new ValidationException("Report code must contain only uppercase letters, numbers, and underscores");
        }
    }

    /**
     * Convert Map to JSON string
     */
    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert List to JSON string
     */
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }
}
