package com.mutindo.reporting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Report Execution DTO for API responses
 */
@Data
@Builder
public class ReportExecutionDto {
    
    private Long id;
    private Long reportDefinitionId;
    private String reportCode;
    private String status;
    private String outputFormat;
    private String parameters;
    private String executedBy;
    private LocalDateTime executionStartTime;
    private LocalDateTime executionEndTime;
    private Long executionDuration;
    private Integer rowsReturned;
    private String filePath;
    private Long fileSize;
    private String errorMessage;
    private Boolean emailSent;
    private String emailRecipients;
    private Boolean isScheduled;
    private Long branchId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
