package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Loan Schedule entity - repayment schedule
 */
@Entity
@Table(name = "loan_schedules", indexes = {
    @Index(name = "idx_schedule_loan", columnList = "loanId"),
    @Index(name = "idx_schedule_due_date", columnList = "dueDate"),
    @Index(name = "idx_schedule_status", columnList = "status"),
    @Index(name = "idx_schedule_installment", columnList = "installmentNumber")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoanSchedule extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "loan_id", nullable = false, length = 36)
    private String loanId;

    @NotNull
    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @Column(name = "principal_due", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalDue;

    @NotNull
    @Column(name = "interest_due", nullable = false, precision = 19, scale = 2)
    private BigDecimal interestDue;

    @Column(name = "fees_due", precision = 19, scale = 2)
    private BigDecimal feesDue = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_due", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDue;

    @Column(name = "principal_paid", precision = 19, scale = 2)
    private BigDecimal principalPaid = BigDecimal.ZERO;

    @Column(name = "interest_paid", precision = 19, scale = 2)
    private BigDecimal interestPaid = BigDecimal.ZERO;

    @Column(name = "fees_paid", precision = 19, scale = 2)
    private BigDecimal feesPaid = BigDecimal.ZERO;

    @Column(name = "total_paid", precision = 19, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "outstanding_amount", precision = 19, scale = 2)
    private BigDecimal outstandingAmount;

    @NotBlank
    @Size(max = 32)
    @Column(name = "status", nullable = false, length = 32)
    private String status; // PENDING, PAID, OVERDUE, PARTIAL

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "days_overdue")
    private Integer daysOverdue = 0;

    @Column(name = "penalty_amount", precision = 19, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @PrePersist
    @PreUpdate
    protected void calculateOutstanding() {
        if (totalDue != null && totalPaid != null) {
            outstandingAmount = totalDue.subtract(totalPaid);
        }
    }
}
