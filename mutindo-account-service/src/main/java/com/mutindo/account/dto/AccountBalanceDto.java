package com.mutindo.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account balance DTO - focused on balance information and limits
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceDto {
    
    private String accountId;
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal blockedAmount; // Funds on hold
    private String currency;
    
    // Limits and thresholds
    private BigDecimal dailyWithdrawalLimit;
    private BigDecimal remainingDailyLimit;
    private BigDecimal minimumBalance;
    private BigDecimal overdraftLimit;
    private BigDecimal overdraftUsed;
    
    // Balance tracking
    private LocalDateTime lastUpdated;
    private LocalDateTime lastTransactionDate;
    
    // Computed fields for UI
    private Boolean isOverdrawn;
    private Boolean isBelowMinimum;
    private Boolean hasAvailableFunds;
    private BigDecimal effectiveAvailableBalance; // Available minus minimum balance
}
