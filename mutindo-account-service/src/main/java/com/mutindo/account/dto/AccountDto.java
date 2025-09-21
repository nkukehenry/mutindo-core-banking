package com.mutindo.account.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Account DTO for API responses - focused on essential account information
 */
@Data
@Builder
public class AccountDto {
    
    private String id;
    private String accountNumber;
    private String customerId;
    private String productCode;
    private String branchId;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String status; // ACTIVE, INACTIVE, DORMANT, FROZEN, CLOSED
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private String closedBy;
    private String closureReason;
    
    // Account limits and settings
    private BigDecimal dailyWithdrawalLimit;
    private BigDecimal minimumBalance;
    private BigDecimal overdraftLimit;
    
    // Custom fields (dynamic)
    private Map<String, Object> customData;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    // Computed fields for UI
    private String customerName;
    private String branchName;
    private String productName;
    private BigDecimal totalTransactionsToday;
    private Integer transactionCountToday;
    private Boolean hasActiveLoans;
    private String lastTransactionDate;
}
