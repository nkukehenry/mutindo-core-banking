package com.mutindo.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Customer DTO for API responses - focused on essential customer information
 */
@Data
@Builder
public class CustomerDto {
    
    private String id;
    private String customerType; // INDIVIDUAL, BUSINESS, GROUP
    private String firstName;
    private String lastName;
    private String legalName; // For business customers
    private String nationalId;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private String phone;
    private String primaryBranchId;
    private String kycStatus;
    private BigDecimal riskScore;
    private String address;
    private String occupation;
    private BigDecimal monthlyIncome;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    // Custom fields (dynamic)
    private Map<String, Object> customData;
    
    // Computed fields for UI
    private String fullName;
    private String branchName;
    private Integer accountCount;
    private Integer activeLoansCount;
}
