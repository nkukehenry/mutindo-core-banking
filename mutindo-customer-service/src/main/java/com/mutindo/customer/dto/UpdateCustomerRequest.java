package com.mutindo.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Update customer request DTO - allows partial updates
 */
@Data
@Builder
public class UpdateCustomerRequest {
    
    @Size(max = 128, message = "First name must be at most 128 characters")
    private String firstName;
    
    @Size(max = 128, message = "Last name must be at most 128 characters")
    private String lastName;
    
    @Size(max = 255, message = "Legal name must be at most 255 characters")
    private String legalName;
    
    @Email(message = "Invalid email format")
    @Size(max = 128, message = "Email must be at most 128 characters")
    private String email;
    
    @Size(max = 32, message = "Phone number must be at most 32 characters")
    private String phone;
    
    @Size(max = 1000, message = "Address must be at most 1000 characters")
    private String address;
    
    @Size(max = 64, message = "Occupation must be at most 64 characters")
    private String occupation;
    
    private BigDecimal monthlyIncome;
    
    // Custom fields updates
    private Map<String, Object> customData;
    
    // Note: Sensitive fields like nationalId, dateOfBirth cannot be updated via this endpoint
    // They require separate verification processes
}
