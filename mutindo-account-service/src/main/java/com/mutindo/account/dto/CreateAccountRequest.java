package com.mutindo.account.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Create account request DTO - comprehensive validation for account opening
 */
@Data
@Builder
public class CreateAccountRequest {
    
    @NotBlank(message = "Customer ID is required")
    @Size(max = 36, message = "Customer ID must be at most 36 characters")
    private String customerId;
    
    @NotBlank(message = "Product code is required")
    @Size(max = 64, message = "Product code must be at most 64 characters")
    private String productCode;
    
    @NotBlank(message = "Branch ID is required")
    @Size(max = 36, message = "Branch ID must be at most 36 characters")
    private String branchId;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 8, message = "Currency must be at most 8 characters")
    private String currency;
    
    @DecimalMin(value = "0.0", message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;
    
    // Account limits (optional - defaults from product if not specified)
    @DecimalMin(value = "0.0", message = "Daily withdrawal limit must be positive")
    private BigDecimal dailyWithdrawalLimit;
    
    @DecimalMin(value = "0.0", message = "Minimum balance must be positive")
    private BigDecimal minimumBalance;
    
    @DecimalMin(value = "0.0", message = "Overdraft limit must be positive")
    private BigDecimal overdraftLimit;
    
    // Custom fields (dynamic based on product configuration)
    private Map<String, Object> customData;
    
    // Account opening purpose and documentation
    @Size(max = 500, message = "Account purpose must be at most 500 characters")
    private String accountPurpose;
    
    @Size(max = 64, message = "Reference number must be at most 64 characters")
    private String referenceNumber; // External reference for account opening
}
