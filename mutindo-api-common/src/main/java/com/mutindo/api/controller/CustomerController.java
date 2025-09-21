package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse; // Reusing existing response wrapper
import com.mutindo.common.dto.PaginatedResponse; // Reusing existing pagination
import com.mutindo.customer.dto.*; // Reusing existing customer DTOs
import com.mutindo.customer.dto.CreateCustomerRequest;
import com.mutindo.customer.dto.CustomerDto;
import com.mutindo.customer.dto.CustomerSearchRequest;
import com.mutindo.customer.dto.UpdateCustomerRequest;
import com.mutindo.customer.service.ICustomerService; // Reusing existing service interface
import com.mutindo.logging.annotation.AuditLog; // Reusing existing audit logging
import com.mutindo.logging.annotation.PerformanceLog; // Reusing existing performance logging
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Customer REST API controller
 * Reuses existing customer service and response infrastructure
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customers", description = "Customer management operations")
public class CustomerController {

    private final ICustomerService customerService; // Reusing existing service interface

    /**
     * Create new customer
     */
    @PostMapping
    @Operation(summary = "Create customer", description = "Create a new customer with KYC validation")
    @PreAuthorize("hasRole('ROLE_CUSTOMERS_CREATE')") // Security check
    @AuditLog // Reusing existing audit logging
    @PerformanceLog // Reusing existing performance logging
    public ResponseEntity<BaseResponse<CustomerDto>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        log.info("Creating customer via API - Type: {}", request.getCustomerType());

        try {
            // Use existing customer service
            CustomerDto customer = customerService.createCustomer(request);
            
            log.info("Customer created successfully via API: {}", customer.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(customer, "Customer created successfully"));
            
        } catch (Exception e) {
            log.error("Failed to create customer via API", e);
            throw e; // Let global exception handler manage the response
        }
    }

    /**
     * Get customer by ID
     */
    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer", description = "Get customer by ID with branch access validation")
    @PreAuthorize("hasRole('ROLE_CUSTOMERS_READ')") // Security check
    @PerformanceLog
    public ResponseEntity<BaseResponse<CustomerDto>> getCustomer(@PathVariable String customerId) {
        log.debug("Getting customer via API: {}", customerId);

        try {
            // Convert String ID to Long for service call
            Long customerIdLong = Long.parseLong(customerId);
            Optional<CustomerDto> customerOpt = customerService.getCustomerById(customerIdLong);
            
            if (customerOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success(customerOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("Customer not found"));
            }
            
        } catch (Exception e) {
            log.error("Failed to get customer via API: {}", customerId, e);
            throw e; // Let global exception handler manage the response
        }
    }

    /**
     * Update customer information
     */
    @PutMapping("/{customerId}")
    @Operation(summary = "Update customer", description = "Update customer information")
    @PreAuthorize("hasRole('ROLE_CUSTOMERS_UPDATE')") // Security check
    @AuditLog
    @PerformanceLog
    public ResponseEntity<BaseResponse<CustomerDto>> updateCustomer(
            @PathVariable String customerId, 
            @Valid @RequestBody UpdateCustomerRequest request) {
        
        log.info("Updating customer via API: {}", customerId);

        try {
            // Convert String ID to Long for service call
            Long customerIdLong = Long.parseLong(customerId);
            CustomerDto updatedCustomer = customerService.updateCustomer(customerIdLong, request);
            
            log.info("Customer updated successfully via API: {}", customerId);
            return ResponseEntity.ok(BaseResponse.success(updatedCustomer, "Customer updated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to update customer via API: {}", customerId, e);
            throw e;
        }
    }

    /**
     * Search customers with pagination
     */
    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search customers with pagination and filtering")
    @PreAuthorize("hasRole('ROLE_CUSTOMERS_READ')") // Security check
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<CustomerDto>>> searchCustomers(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String kycStatus,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Searching customers via API - Term: {}", searchTerm);

        try {
            // Build search request (small method)
            CustomerSearchRequest searchRequest = buildSearchRequest(searchTerm, customerType, branchId, kycStatus, active);
            
            // Use existing customer service
            PaginatedResponse<CustomerDto> customers = customerService.searchCustomers(searchRequest, pageable);
            
            log.debug("Found {} customers via API", customers.getTotalElements());
            return ResponseEntity.ok(BaseResponse.success(customers));
            
        } catch (Exception e) {
            log.error("Failed to search customers via API", e);
            throw e;
        }
    }

    /**
     * Deactivate customer
     */
    @DeleteMapping("/{customerId}")
    @Operation(summary = "Deactivate customer", description = "Deactivate customer account")
    @PreAuthorize("hasRole('ROLE_CUSTOMERS_DELETE')") // Security check
    @AuditLog
    public ResponseEntity<BaseResponse<Void>> deactivateCustomer(
            @PathVariable String customerId,
            @RequestParam String reason) {
        
        log.info("Deactivating customer via API: {} - Reason: {}", customerId, reason);

        try {
            // Convert String ID to Long for service call
            Long customerIdLong = Long.parseLong(customerId);
            customerService.deactivateCustomer(customerIdLong, reason);
            
            log.info("Customer deactivated successfully via API: {}", customerId);
            return ResponseEntity.ok(BaseResponse.success(null, "Customer deactivated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to deactivate customer via API: {}", customerId, e);
            throw e;
        }
    }

    /**
     * Update customer KYC status
     */
    @PatchMapping("/{customerId}/kyc")
    @Operation(summary = "Update KYC status", description = "Update customer KYC status and documents")
    @PreAuthorize("hasRole('ROLE_CUSTOMERS_KYC_UPDATE')") // Security check
    @AuditLog
    @PerformanceLog
    public ResponseEntity<BaseResponse<Void>> updateKycStatus(
            @PathVariable String customerId,
            @RequestParam String kycStatus,
            @RequestBody(required = false) Object documents) {
        
        log.info("Updating KYC status via API - Customer: {} - Status: {}", customerId, kycStatus);

        try {
            // Convert String ID to Long for service call
            Long customerIdLong = Long.parseLong(customerId);
            customerService.updateKycStatus(customerIdLong, kycStatus, documents);
            
            log.info("KYC status updated successfully via API: {}", customerId);
            return ResponseEntity.ok(BaseResponse.success(null, "KYC status updated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to update KYC status via API: {}", customerId, e);
            throw e;
        }
    }

    /**
     * Check if customer exists by national ID
     */
    @GetMapping("/exists/national-id/{nationalId}")
    @Operation(summary = "Check customer existence", description = "Check if customer exists by national ID")
    @PreAuthorize("hasRole('ROLE_CUSTOMERS_READ')") // Security check
    @PerformanceLog
    public ResponseEntity<BaseResponse<Boolean>> checkCustomerExistence(@PathVariable String nationalId) {
        log.debug("Checking customer existence via API - National ID: {}", nationalId);

        try {
            // Use existing customer service
            boolean exists = customerService.existsByNationalId(nationalId);
            
            return ResponseEntity.ok(BaseResponse.success(exists));
            
        } catch (Exception e) {
            log.error("Failed to check customer existence via API", e);
            throw e;
        }
    }

    // Private helper methods (small and focused)

    /**
     * Build customer search request from query parameters
     */
    private CustomerSearchRequest buildSearchRequest(String searchTerm, String customerType, 
                                                   String branchId, String kycStatus, Boolean active) {
        return CustomerSearchRequest.builder()
                .searchTerm(searchTerm)
                .customerType(customerType)
                .branchId(branchId) // Will be filtered by service based on user context
                .kycStatus(kycStatus)
                .active(active)
                .build();
    }
}
