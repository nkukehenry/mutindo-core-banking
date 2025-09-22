package com.mutindo.reporting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Generate Report request DTO
 */
@Data
@Builder
public class GenerateReportRequest {
    
    @NotBlank(message = "Report code is required")
    @Size(max = 64, message = "Report code must not exceed 64 characters")
    private String reportCode;
    
    @Size(max = 32, message = "Output format must not exceed 32 characters")
    private String outputFormat; // PDF, EXCEL, CSV
    
    private Map<String, Object> parameters; // Report parameters
    
    private List<String> emailRecipients; // Email recipients for report delivery
    
    private Boolean sendEmail; // Whether to send report via email
    
    private String emailSubject; // Custom email subject
    
    private String emailMessage; // Custom email message
    
    private Long branchId; // Branch context for multi-branch reports
    
    private Boolean isScheduled; // Whether this is a scheduled execution
}
