package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Report Execution entity - tracks report generation history
 */
@Entity
@Table(name = "report_executions", indexes = {
    @Index(name = "idx_report_exec_report", columnList = "reportDefinitionId"),
    @Index(name = "idx_report_exec_user", columnList = "executedBy"),
    @Index(name = "idx_report_exec_status", columnList = "status"),
    @Index(name = "idx_report_exec_created", columnList = "createdAt")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReportExecution extends BaseEntity {

    @NotNull
    @Column(name = "report_definition_id", nullable = false)
    private Long reportDefinitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_definition_id", insertable = false, updatable = false)
    private ReportDefinition reportDefinition;

    @NotBlank
    @Size(max = 64)
    @Column(name = "report_code", nullable = false, length = 64)
    private String reportCode;

    @NotBlank
    @Size(max = 32)
    @Column(name = "status", nullable = false, length = 32)
    private String status; // PENDING, RUNNING, COMPLETED, FAILED, CANCELLED

    @Size(max = 32)
    @Column(name = "output_format", length = 32)
    private String outputFormat; // PDF, EXCEL, CSV

    @Column(name = "parameters", columnDefinition = "JSON")
    private String parameters; // JSON string of execution parameters

    @Column(name = "executed_by", length = 64)
    private String executedBy; // User ID who executed the report

    @Column(name = "execution_start_time")
    private LocalDateTime executionStartTime;

    @Column(name = "execution_end_time")
    private LocalDateTime executionEndTime;

    @Column(name = "execution_duration")
    private Long executionDuration; // Duration in milliseconds

    @Column(name = "rows_returned")
    private Integer rowsReturned;

    @Column(name = "file_path", length = 500)
    private String filePath; // Path to generated report file

    @Column(name = "file_size")
    private Long fileSize; // File size in bytes

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "email_sent")
    private Boolean emailSent = false;

    @Column(name = "email_recipients", columnDefinition = "JSON")
    private String emailRecipients; // JSON array of email addresses

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false; // Whether this was a scheduled execution

    @Column(name = "branch_id")
    private Long branchId; // Branch context for multi-branch reports
}
