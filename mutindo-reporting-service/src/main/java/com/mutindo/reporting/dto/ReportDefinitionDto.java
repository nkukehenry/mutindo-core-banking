package com.mutindo.reporting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Report Definition DTO for API responses
 */
@Data
@Builder
public class ReportDefinitionDto {
    
    private Long id;
    private String reportCode;
    private String reportName;
    private String description;
    private String category;
    private String reportType;
    private String sqlQuery;
    private String parameters;
    private List<String> outputFormats;
    private String templatePath;
    private Boolean isSystem;
    private Boolean isScheduled;
    private String scheduleCron;
    private List<String> emailRecipients;
    private Boolean active;
    private String accessRole;
    private Integer cacheDuration;
    private Integer maxRows;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
