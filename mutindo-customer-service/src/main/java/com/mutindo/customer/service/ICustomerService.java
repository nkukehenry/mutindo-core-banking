package com.mutindo.customer.service;

import com.mutindo.customer.dto.CustomerDto;
import com.mutindo.customer.dto.CreateCustomerRequest;
import com.mutindo.customer.dto.UpdateCustomerRequest;
import com.mutindo.customer.dto.CustomerSearchRequest;
import com.mutindo.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Customer service interface for polymorphic customer operations
 * Follows our established pattern of interface-driven design
 */
public interface ICustomerService {
    
    /**
     * Create new customer with KYC validation
     * @param request Customer creation request
     * @return Created customer information
     */
    CustomerDto createCustomer(CreateCustomerRequest request);
    
    /**
     * Get customer by ID with branch access validation
     * @param customerId Customer ID
     * @return Customer information if authorized
     */
    Optional<CustomerDto> getCustomerById(Long customerId);
    
    /**
     * Update customer information
     * @param customerId Customer ID
     * @param request Update request
     * @return Updated customer information
     */
    CustomerDto updateCustomer(Long customerId, UpdateCustomerRequest request);
    
    /**
     * Search customers with pagination and branch filtering
     * @param searchRequest Search criteria
     * @param pageable Pagination parameters
     * @return Paginated customer results
     */
    PaginatedResponse<CustomerDto> searchCustomers(CustomerSearchRequest searchRequest, Pageable pageable);
    
    /**
     * Get customers by branch (for branch users)
     * @param branchId Branch ID
     * @param pageable Pagination parameters
     * @return Paginated customers for branch
     */
    PaginatedResponse<CustomerDto> getCustomersByBranch(Long branchId, Pageable pageable);
    
    /**
     * Deactivate customer (soft delete)
     * @param customerId Customer ID
     * @param reason Deactivation reason
     */
    void deactivateCustomer(Long customerId, String reason);
    
    /**
     * Activate customer
     * @param customerId Customer ID
     */
    void activateCustomer(Long customerId);
    
    /**
     * Update KYC status
     * @param customerId Customer ID
     * @param kycStatus New KYC status
     * @param documents KYC documents
     */
    void updateKycStatus(Long customerId, String kycStatus, Object documents);
    
    /**
     * Check if customer exists by national ID
     * @param nationalId National ID to check
     * @return true if customer exists
     */
    boolean existsByNationalId(String nationalId);
    
    /**
     * Async customer risk assessment
     * @param customerId Customer ID
     * @return Future with risk score calculation
     */
    CompletableFuture<Void> calculateRiskScoreAsync(Long customerId);
}
