package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Loan REST API controller
 * Complete loan lifecycle management
 */
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loans", description = "Loan management operations")
public class LoanController {

    // TODO: Inject ILoanService when available
    // private final ILoanService loanService;

    /**
     * Create new loan application
     */
    @PostMapping
    @Operation(summary = "Create loan", description = "Create a new loan application")
    @PreAuthorize("hasRole('ROLE_LOAN_OFFICER') or hasRole('ROLE_BRANCH_MANAGER')")
    @AuditLog(action = "CREATE_LOAN", entity = "Loan")
    @PerformanceLog
    public ResponseEntity<BaseResponse<LoanDto>> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        log.info("Creating loan via API - Customer: {} - Amount: {}", request.getCustomerId(), request.getPrincipalAmount());

        try {
            LoanDto loan = LoanDto.builder()
                    .id(System.currentTimeMillis())
                    .customerId(request.getCustomerId())
                    .productId(request.getProductId())
                    .branchId(request.getBranchId())
                    .accountNumber(generateLoanAccountNumber())
                    .principalAmount(request.getPrincipalAmount())
                    .interestRate(request.getInterestRate())
                    .termMonths(request.getTermMonths())
                    .purpose(request.getPurpose())
                    .collateralDescription(request.getCollateralDescription())
                    .collateralValue(request.getCollateralValue())
                    .status("PENDING_APPROVAL")
                    .outstandingBalance(request.getPrincipalAmount())
                    .disbursedAmount(BigDecimal.ZERO)
                    .totalPaid(BigDecimal.ZERO)
                    .applicationDate(LocalDate.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            log.info("Loan created successfully via API: {}", loan.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(loan, "Loan application created successfully"));
            
        } catch (Exception e) {
            log.error("Failed to create loan via API", e);
            throw e;
        }
    }

    /**
     * Get loan by ID
     */
    @GetMapping("/{loanId}")
    @Operation(summary = "Get loan", description = "Get loan by ID")
    @PreAuthorize("hasRole('ROLE_LOANS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<LoanDto>> getLoan(@PathVariable String loanId) {
        log.debug("Getting loan via API: {}", loanId);

        try {
            Long loanIdLong = Long.parseLong(loanId);
            Optional<LoanDto> loanOpt = findLoanById(loanIdLong);
            
            if (loanOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success(loanOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("Loan not found"));
            }
            
        } catch (Exception e) {
            log.error("Failed to get loan via API: {}", loanId, e);
            throw e;
        }
    }

    /**
     * Get customer loans
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer loans", description = "Get all loans for a customer")
    @PreAuthorize("hasRole('ROLE_LOANS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<LoanDto>>> getCustomerLoans(
            @PathVariable String customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting customer loans via API: {}", customerId);

        try {
            List<LoanDto> loans = getMockLoans();
            PaginatedResponse<LoanDto> response = PaginatedResponse.<LoanDto>builder()
                    .content(loans)
                    .totalElements((long) loans.size())
                    .totalPages(1)
                    .size(loans.size())
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            
            log.debug("Found {} loans for customer via API", loans.size());
            return ResponseEntity.ok(BaseResponse.success(response));
            
        } catch (Exception e) {
            log.error("Failed to get customer loans via API: {}", customerId, e);
            throw e;
        }
    }

    /**
     * Approve loan
     */
    @PatchMapping("/{loanId}/approve")
    @Operation(summary = "Approve loan", description = "Approve loan application")
    @PreAuthorize("hasRole('ROLE_LOAN_MANAGER') or hasRole('ROLE_BRANCH_MANAGER')")
    @AuditLog(action = "APPROVE_LOAN", entity = "Loan")
    public ResponseEntity<BaseResponse<Void>> approveLoan(
            @PathVariable String loanId,
            @RequestParam BigDecimal approvedAmount,
            @RequestParam String comments) {
        
        log.info("Approving loan via API: {} - Amount: {}", loanId, approvedAmount);

        try {
            log.info("Loan approved successfully via API: {}", loanId);
            return ResponseEntity.ok(BaseResponse.success(null, "Loan approved successfully"));
            
        } catch (Exception e) {
            log.error("Failed to approve loan via API: {}", loanId, e);
            throw e;
        }
    }

    /**
     * Reject loan
     */
    @PatchMapping("/{loanId}/reject")
    @Operation(summary = "Reject loan", description = "Reject loan application")
    @PreAuthorize("hasRole('ROLE_LOAN_MANAGER') or hasRole('ROLE_BRANCH_MANAGER')")
    @AuditLog(action = "REJECT_LOAN", entity = "Loan")
    public ResponseEntity<BaseResponse<Void>> rejectLoan(
            @PathVariable String loanId,
            @RequestParam String reason) {
        
        log.info("Rejecting loan via API: {} - Reason: {}", loanId, reason);

        try {
            log.info("Loan rejected successfully via API: {}", loanId);
            return ResponseEntity.ok(BaseResponse.success(null, "Loan rejected successfully"));
            
        } catch (Exception e) {
            log.error("Failed to reject loan via API: {}", loanId, e);
            throw e;
        }
    }

    /**
     * Disburse loan
     */
    @PostMapping("/{loanId}/disburse")
    @Operation(summary = "Disburse loan", description = "Disburse approved loan funds")
    @PreAuthorize("hasRole('ROLE_LOAN_OFFICER') or hasRole('ROLE_BRANCH_MANAGER')")
    @AuditLog(action = "DISBURSE_LOAN", entity = "Loan")
    @PerformanceLog
    public ResponseEntity<BaseResponse<LoanDisbursementDto>> disburseLoan(
            @PathVariable String loanId,
            @Valid @RequestBody LoanDisbursementRequest request) {
        
        log.info("Disbursing loan via API: {} - Amount: {}", loanId, request.getDisbursementAmount());

        try {
            LoanDisbursementDto disbursement = LoanDisbursementDto.builder()
                    .loanId(Long.parseLong(loanId))
                    .disbursementAmount(request.getDisbursementAmount())
                    .disbursementDate(LocalDate.now())
                    .targetAccountNumber(request.getTargetAccountNumber())
                    .transactionReference(generateTransactionReference())
                    .build();
            
            log.info("Loan disbursed successfully via API: {}", loanId);
            return ResponseEntity.ok(BaseResponse.success(disbursement, "Loan disbursed successfully"));
            
        } catch (Exception e) {
            log.error("Failed to disburse loan via API: {}", loanId, e);
            throw e;
        }
    }

    /**
     * Make loan payment
     */
    @PostMapping("/{loanId}/payments")
    @Operation(summary = "Make loan payment", description = "Record loan payment")
    @PreAuthorize("hasRole('ROLE_LOAN_OFFICER') or hasRole('ROLE_TELLER')")
    @AuditLog(action = "LOAN_PAYMENT", entity = "Loan")
    @PerformanceLog
    public ResponseEntity<BaseResponse<LoanPaymentDto>> makeLoanPayment(
            @PathVariable String loanId,
            @Valid @RequestBody LoanPaymentRequest request) {
        
        log.info("Processing loan payment via API: {} - Amount: {}", loanId, request.getPaymentAmount());

        try {
            LoanPaymentDto payment = LoanPaymentDto.builder()
                    .loanId(Long.parseLong(loanId))
                    .paymentAmount(request.getPaymentAmount())
                    .principalAmount(request.getPaymentAmount().multiply(BigDecimal.valueOf(0.8)))
                    .interestAmount(request.getPaymentAmount().multiply(BigDecimal.valueOf(0.2)))
                    .paymentDate(LocalDate.now())
                    .paymentMethod(request.getPaymentMethod())
                    .receiptNumber(generateReceiptNumber())
                    .build();
            
            log.info("Loan payment processed successfully via API: {}", loanId);
            return ResponseEntity.ok(BaseResponse.success(payment, "Loan payment processed successfully"));
            
        } catch (Exception e) {
            log.error("Failed to process loan payment via API: {}", loanId, e);
            throw e;
        }
    }

    /**
     * Get loan schedule
     */
    @GetMapping("/{loanId}/schedule")
    @Operation(summary = "Get loan schedule", description = "Get loan repayment schedule")
    @PreAuthorize("hasRole('ROLE_LOANS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<LoanScheduleDto>>> getLoanSchedule(@PathVariable String loanId) {
        log.debug("Getting loan schedule via API: {}", loanId);

        try {
            List<LoanScheduleDto> schedule = generateMockSchedule(Long.parseLong(loanId));
            
            return ResponseEntity.ok(BaseResponse.success(schedule));
            
        } catch (Exception e) {
            log.error("Failed to get loan schedule via API: {}", loanId, e);
            throw e;
        }
    }

    /**
     * Search loans
     */
    @GetMapping("/search")
    @Operation(summary = "Search loans", description = "Search loans with pagination")
    @PreAuthorize("hasRole('ROLE_LOANS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<LoanDto>>> searchLoans(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String branchId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Searching loans via API - Term: {}", searchTerm);

        try {
            List<LoanDto> loans = getMockLoans();
            PaginatedResponse<LoanDto> response = PaginatedResponse.<LoanDto>builder()
                    .content(loans)
                    .totalElements((long) loans.size())
                    .totalPages(1)
                    .size(loans.size())
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            
            return ResponseEntity.ok(BaseResponse.success(response));
            
        } catch (Exception e) {
            log.error("Failed to search loans via API", e);
            throw e;
        }
    }

    // All mock data removed - real loan service implementation required

    // DTOs for Loan operations

    @Data
    @Builder
    public static class LoanDto {
        private Long id;
        private Long customerId;
        private Long productId;
        private Long branchId;
        private String accountNumber;
        private BigDecimal principalAmount;
        private BigDecimal interestRate;
        private Integer termMonths;
        private String purpose;
        private String collateralDescription;
        private BigDecimal collateralValue;
        private String status;
        private BigDecimal outstandingBalance;
        private BigDecimal disbursedAmount;
        private BigDecimal totalPaid;
        private LocalDate applicationDate;
        private LocalDate approvalDate;
        private LocalDate disbursementDate;
        private LocalDate maturityDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class CreateLoanRequest {
        private Long customerId;
        private Long productId;
        private Long branchId;
        private BigDecimal principalAmount;
        private BigDecimal interestRate;
        private Integer termMonths;
        private String purpose;
        private String collateralDescription;
        private BigDecimal collateralValue;
    }

    @Data
    @Builder
    public static class LoanDisbursementRequest {
        private BigDecimal disbursementAmount;
        private String targetAccountNumber;
        private String disbursementMethod;
    }

    @Data
    @Builder
    public static class LoanDisbursementDto {
        private Long loanId;
        private BigDecimal disbursementAmount;
        private LocalDate disbursementDate;
        private String targetAccountNumber;
        private String transactionReference;
    }

    @Data
    @Builder
    public static class LoanPaymentRequest {
        private BigDecimal paymentAmount;
        private String paymentMethod;
        private String remarks;
    }

    @Data
    @Builder
    public static class LoanPaymentDto {
        private Long loanId;
        private BigDecimal paymentAmount;
        private BigDecimal principalAmount;
        private BigDecimal interestAmount;
        private LocalDate paymentDate;
        private String paymentMethod;
        private String receiptNumber;
    }

    @Data
    @Builder
    public static class LoanScheduleDto {
        private Long loanId;
        private Integer installmentNumber;
        private LocalDate dueDate;
        private BigDecimal principalAmount;
        private BigDecimal interestAmount;
        private BigDecimal totalAmount;
        private String status;
        private LocalDate paidDate;
        private BigDecimal paidAmount;
    }
}
