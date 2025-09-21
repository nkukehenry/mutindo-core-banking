package com.mutindo.customer.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Create customer request DTO - comprehensive validation
 */
@Data
@Builder
public class CreateCustomerRequest {
    
    @NotBlank(message = "Customer type is required")
    @Pattern(regexp = "INDIVIDUAL|BUSINESS|GROUP", message = "Invalid customer type")
    private String customerType;
    
    @Size(max = 128, message = "First name must be at most 128 characters")
    private String firstName; // Required for INDIVIDUAL
    
    @Size(max = 128, message = "Last name must be at most 128 characters")
    private String lastName; // Required for INDIVIDUAL
    
    @Size(max = 255, message = "Legal name must be at most 255 characters")
    private String legalName; // Required for BUSINESS/GROUP
    
    @Size(max = 64, message = "National ID must be at most 64 characters")
    private String nationalId;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth; // Required for INDIVIDUAL
    
    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Invalid gender")
    private String gender; // For INDIVIDUAL
    
    @Email(message = "Invalid email format")
    @Size(max = 128, message = "Email must be at most 128 characters")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 32, message = "Phone number must be at most 32 characters")
    private String phone;
    
    @NotBlank(message = "Primary branch ID is required")
    @Size(max = 36, message = "Branch ID must be at most 36 characters")
    private String primaryBranchId;
    
    @Size(max = 1000, message = "Address must be at most 1000 characters")
    private String address;
    
    @Size(max = 64, message = "Occupation must be at most 64 characters")
    private String occupation;
    
    @DecimalMin(value = "0.0", message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;
    
    // Custom fields (dynamic based on branch configuration)
    private Map<String, Object> customData;
    
    // Initial KYC documents
    private Map<String, Object> kycDocuments;
}
