package com.mutindo.reporting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Report generation response DTO
 */
@Data
@Builder
public class ReportResponse {
    
    private Long executionId;
    private String reportCode;
    private String reportName;
    private String status;
    private String outputFormat;
    private String filePath;
    private Long fileSize;
    private Integer rowsReturned;
    private Long executionDuration;
    private LocalDateTime executionStartTime;
    private LocalDateTime executionEndTime;
    private String errorMessage;
    private Boolean emailSent;
    private String downloadUrl; // URL to download the generated report
}
