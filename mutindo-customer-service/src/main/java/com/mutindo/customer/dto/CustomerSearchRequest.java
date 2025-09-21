package com.mutindo.customer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Customer search request DTO - small and focused search criteria
 */
@Data
@Builder
public class CustomerSearchRequest {
    
    private String searchTerm; // Name, phone, national ID, email
    private String customerType; // INDIVIDUAL, BUSINESS, GROUP
    private String branchId; // Branch filter (auto-applied for branch users)
    private String kycStatus; // KYC status filter
    private Boolean active; // Active/inactive filter
    private String occupation; // Occupation filter
    
    // Date range filters
    private String createdAfter; // ISO date string
    private String createdBefore; // ISO date string
    
    // Financial filters
    private String minMonthlyIncome;
    private String maxMonthlyIncome;
}
