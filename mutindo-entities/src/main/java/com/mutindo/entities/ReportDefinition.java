package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Report Definition entity - stores report templates and configurations
 */
@Entity
@Table(name = "report_definitions", indexes = {
    @Index(name = "idx_report_def_code", columnList = "reportCode", unique = true),
    @Index(name = "idx_report_def_category", columnList = "category"),
    @Index(name = "idx_report_def_active", columnList = "active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinition extends BaseEntity {

    @NotBlank
    @Size(max = 64)
    @Column(name = "report_code", nullable = false, unique = true, length = 64)
    private String reportCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "report_name", nullable = false, length = 255)
    private String reportName;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotBlank
    @Size(max = 64)
    @Column(name = "category", nullable = false, length = 64)
    private String category; // FINANCIAL, OPERATIONAL, COMPLIANCE, CUSTOMER, etc.

    @NotBlank
    @Size(max = 32)
    @Column(name = "report_type", nullable = false, length = 32)
    private String reportType; // TABULAR, CHART, SUMMARY, DETAILED

    @Column(name = "sql_query", columnDefinition = "TEXT")
    private String sqlQuery;

    @Column(name = "parameters", columnDefinition = "JSON")
    private String parameters; // JSON string of report parameters

    @Column(name = "output_formats", columnDefinition = "JSON")
    private String outputFormats; // JSON array of supported formats [PDF, EXCEL, CSV]

    @Column(name = "template_path", length = 500)
    private String templatePath; // Path to report template

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false; // System reports cannot be deleted

    @Column(name = "is_scheduled", nullable = false)
    private Boolean isScheduled = false; // Can be scheduled for automatic generation

    @Column(name = "schedule_cron", length = 100)
    private String scheduleCron; // Cron expression for scheduling

    @Column(name = "email_recipients", columnDefinition = "JSON")
    private String emailRecipients; // JSON array of email addresses

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 64)
    @Column(name = "access_role", length = 64)
    private String accessRole; // Required role to access this report

    @Column(name = "cache_duration", nullable = false)
    private Integer cacheDuration = 0; // Cache duration in minutes (0 = no cache)

    @Column(name = "max_rows", nullable = false)
    private Integer maxRows = 10000; // Maximum rows to return

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false; // Public reports visible to all users
}
