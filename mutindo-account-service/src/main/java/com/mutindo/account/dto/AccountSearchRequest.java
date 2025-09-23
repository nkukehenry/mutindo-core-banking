package com.mutindo.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account search request DTO - small and focused search criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSearchRequest {
    
    private String searchTerm; // Account number, customer name, product code
    private String customerId; // Filter by customer
    private String productCode; // Filter by product
    private String branchId; // Branch filter (auto-applied for branch users)
    private String status; // Account status filter
    private String currency; // Currency filter
    
    // Date range filters
    private String openedAfter; // ISO date string
    private String openedBefore; // ISO date string
    
    // Balance filters
    private String minBalance;
    private String maxBalance;
    
    // Boolean filters
    private Boolean active; // Active/inactive filter
    private Boolean hasOverdraft; // Accounts with overdraft facility
}
