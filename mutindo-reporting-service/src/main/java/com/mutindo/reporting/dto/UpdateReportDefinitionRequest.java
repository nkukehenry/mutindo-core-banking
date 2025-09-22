package com.mutindo.reporting.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Update Report Definition request DTO
 */
@Data
@Builder
public class UpdateReportDefinitionRequest {
    
    @Size(max = 255, message = "Report name must not exceed 255 characters")
    private String reportName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;
    
    @Size(max = 32, message = "Report type must not exceed 32 characters")
    private String reportType;
    
    private String sqlQuery;
    private String parameters; // JSON string
    private List<String> outputFormats;
    private String templatePath;
    private Boolean isScheduled;
    private String scheduleCron;
    private List<String> emailRecipients;
    private String accessRole;
    private Integer cacheDuration;
    private Integer maxRows;
    private Boolean isPublic;
}
