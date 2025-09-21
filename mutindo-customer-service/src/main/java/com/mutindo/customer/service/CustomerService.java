package com.mutindo.customer.service;

import com.mutindo.common.context.BranchContextHolder; // Reusing existing branch context
import com.mutindo.common.dto.PaginatedResponse; // Reusing existing pagination
import com.mutindo.common.enums.CustomerType;
import com.mutindo.customer.dto.*;
import com.mutindo.customer.dto.CreateCustomerRequest;
import com.mutindo.customer.dto.CustomerDto;
import com.mutindo.customer.dto.CustomerSearchRequest;
import com.mutindo.customer.dto.UpdateCustomerRequest;
import com.mutindo.customer.mapper.CustomerMapper;
import com.mutindo.encryption.service.IEncryptionService; // Reusing existing encryption interface
import com.mutindo.entities.Customer;
import com.mutindo.exceptions.BusinessException; // Reusing existing exceptions
import com.mutindo.exceptions.ValidationException; // Reusing existing exceptions
import com.mutindo.logging.annotation.AuditLog; // Reusing existing audit logging
import com.mutindo.logging.annotation.PerformanceLog; // Reusing existing performance logging
import com.mutindo.repositories.BranchRepository; // Reusing existing repositories
import com.mutindo.repositories.CustomerRepository; // Reusing existing repositories
import com.mutindo.validation.validator.PhoneNumberValidator; // Reusing existing validation
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Customer service implementation with KYC and multi-branch support
 * Reuses existing infrastructure: Encryption, Logging, Caching, Repositories, Validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService implements ICustomerService {

    // Reusing existing infrastructure components via interfaces
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final IEncryptionService encryptionService;
    private final PhoneNumberValidator phoneNumberValidator;
    private final CustomerMapper customerMapper;

    /**
     * Create new customer with comprehensive validation and security
     */
    @Override
    @Transactional
    @AuditLog // Reusing existing audit logging
    @PerformanceLog // Reusing existing performance logging
    @CacheEvict(value = "customers", allEntries = true) // Clear customer cache
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer: {} - Type: {}", 
                maskSensitiveData(request.getPhone()), request.getCustomerType());

        try {
            // Validate request (small method with clear purpose)
            validateCreateCustomerRequest(request);
            
            // Check branch access permission (security validation)
            validateBranchAccess(request.getPrimaryBranchId());
            
            // Check for duplicates (small method)
            validateNoDuplicateCustomer(request);
            
            // Create customer entity (small method)
            Customer customer = createCustomerFromRequest(request);
            
            // Encrypt sensitive data (reusing existing encryption)
            encryptSensitiveCustomerData(customer);
            
            // Save customer (using existing repository)
            Customer savedCustomer = customerRepository.save(customer);
            
            // Async operations (non-blocking)
            performPostCreationOperationsAsync(savedCustomer.getId());
            
            log.info("Customer created successfully: {}", savedCustomer.getId());
            return customerMapper.toDto(savedCustomer);

        } catch (Exception e) {
            log.error("Failed to create customer", e);
            throw e; // Re-throw to maintain error handling chain
        }
    }

    /**
     * Get customer by ID with branch access validation
     */
    @Override
    @Cacheable(value = "customers", key = "#customerId") // Cache for performance
    @PerformanceLog
    public Optional<CustomerDto> getCustomerById(Long customerId) {
        log.debug("Getting customer by ID: {}", customerId);

        try {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            
            if (customerOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Customer customer = customerOpt.get();
            
            // Validate branch access (security check)
            validateCustomerAccess(customer);
            
            // Decrypt sensitive data if needed
            decryptSensitiveCustomerData(customer);
            
            // Convert to DTO
            CustomerDto dto = customerMapper.toDto(customer);
            
            // Add computed fields (small method)
            enrichCustomerDto(dto);
            
            return Optional.of(dto);

        } catch (Exception e) {
            log.error("Failed to get customer: {}", customerId, e);
            throw e;
        }
    }

    /**
     * Update customer information with validation
     */
    @Override
    @Transactional
    @AuditLog
    @PerformanceLog
    @CacheEvict(value = "customers", key = "#customerId") // Clear specific customer cache
    public CustomerDto updateCustomer(Long customerId, UpdateCustomerRequest request) {
        log.info("Updating customer: {}", customerId);

        try {
            // Find existing customer
            Customer existingCustomer = findCustomerById(customerId);
            
            // Validate access (security check)
            validateCustomerAccess(existingCustomer);
            
            // Validate update request (small method)
            validateUpdateCustomerRequest(request);
            
            // Update customer fields (small method)
            updateCustomerFields(existingCustomer, request);
            
            // Save updated customer
            Customer savedCustomer = customerRepository.save(existingCustomer);
            
            log.info("Customer updated successfully: {}", customerId);
            return customerMapper.toDto(savedCustomer);

        } catch (Exception e) {
            log.error("Failed to update customer: {}", customerId, e);
            throw e;
        }
    }

    /**
     * Search customers with branch filtering and pagination
     */
    @Override
    @PerformanceLog
    public PaginatedResponse<CustomerDto> searchCustomers(CustomerSearchRequest searchRequest, Pageable pageable) {
        log.debug("Searching customers with criteria: {}", searchRequest.getSearchTerm());

        try {
            // Apply branch filtering based on user context (security)
            String effectiveBranchId = getEffectiveBranchId(searchRequest.getBranchId());
            
            // Execute search (using existing repository methods)
            Page<Customer> customerPage = executeCustomerSearch(searchRequest, effectiveBranchId, pageable);
            
            // Convert to DTOs and enrich
            PaginatedResponse<CustomerDto> response = convertToCustomerDtoPage(customerPage);
            
            log.debug("Found {} customers", response.getTotalElements());
            return response;

        } catch (Exception e) {
            log.error("Failed to search customers", e);
            throw e;
        }
    }

    /**
     * Get customers by branch with access validation
     */
    @Override
    @Cacheable(value = "customersByBranch", key = "#branchId + ':' + #pageable.pageNumber") // Cache branch results
    @PerformanceLog
    public PaginatedResponse<CustomerDto> getCustomersByBranch(Long branchId, Pageable pageable) {
        log.debug("Getting customers for branch: {}", branchId);

        // Validate branch access (security check)
        validateBranchAccess(branchId.toString());
        
        // Use existing repository method
        Page<Customer> customerPage = customerRepository.findByPrimaryBranchId(branchId, pageable);
        
        return convertToCustomerDtoPage(customerPage);
    }

    /**
     * Deactivate customer with audit trail
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"customers", "customersByBranch"}, allEntries = true)
    public void deactivateCustomer(Long customerId, String reason) {
        log.info("Deactivating customer: {} - Reason: {}", customerId, reason);

        Customer customer = findCustomerById(customerId);
        validateCustomerAccess(customer);
        
        // Validate can be deactivated (small method)
        validateCanDeactivateCustomer(customer);
        
        customer.setActive(false);
        customerRepository.save(customer);
        
        log.info("Customer deactivated successfully: {}", customerId);
    }

    /**
     * Update KYC status with document management
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = "customers", key = "#customerId")
    public void updateKycStatus(Long customerId, String kycStatus, Object documents) {
        log.info("Updating KYC status for customer: {} - Status: {}", customerId, kycStatus);

        Customer customer = findCustomerById(customerId);
        validateCustomerAccess(customer);
        
        // Update KYC information (small method)
        updateCustomerKyc(customer, kycStatus, documents);
        
        customerRepository.save(customer);
        
        log.info("KYC status updated successfully for customer: {}", customerId);
    }

    /**
     * Check customer existence by national ID
     */
    @Override
    @Cacheable(value = "customerExists", key = "#nationalId") // Cache existence checks
    public boolean existsByNationalId(String nationalId) {
        return customerRepository.existsByNationalId(nationalId);
    }

    /**
     * Async risk score calculation
     */
    @Override
    @Async
    public CompletableFuture<Void> calculateRiskScoreAsync(Long customerId) {
        log.info("Calculating risk score asynchronously for customer: {}", customerId);

        try {
            // Calculate risk score based on business rules (placeholder implementation)
            BigDecimal riskScore = calculateCustomerRiskScore(customerId);
            
            // Update customer record
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new BusinessException("Customer not found", "CUSTOMER_NOT_FOUND"));
            
            customer.setRiskScore(riskScore);
            customerRepository.save(customer);
            
            log.info("Risk score calculated and updated for customer: {}", customerId);
            
        } catch (Exception e) {
            log.error("Failed to calculate risk score for customer: {}", customerId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // Private helper methods (small and focused)

    /**
     * Validate create customer request
     */
    private void validateCreateCustomerRequest(CreateCustomerRequest request) {
        // Validate customer type specific requirements
        if ("INDIVIDUAL".equals(request.getCustomerType())) {
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                throw new ValidationException("First name is required for individual customers");
            }
            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                throw new ValidationException("Last name is required for individual customers");
            }
        } else if ("BUSINESS".equals(request.getCustomerType()) || "GROUP".equals(request.getCustomerType())) {
            if (request.getLegalName() == null || request.getLegalName().trim().isEmpty()) {
                throw new ValidationException("Legal name is required for business/group customers");
            }
        }
        
        // Validate phone number (reusing existing validator)
        phoneNumberValidator.validate(request.getPhone());
    }

    /**
     * Validate branch access based on user context
     */
    private void validateBranchAccess(String branchId) {
        Long branchIdLong = branchId != null ? Long.parseLong(branchId) : null;
        if (!BranchContextHolder.canCurrentUserAccessBranch(branchIdLong)) {
            throw new BusinessException("Access denied to branch: " + branchId, "BRANCH_ACCESS_DENIED");
        }
        
        // Verify branch exists
        if (!branchRepository.existsById(Long.parseLong(branchId))) {
            throw new BusinessException("Branch not found: " + branchId, "BRANCH_NOT_FOUND");
        }
    }

    /**
     * Check for duplicate customers
     */
    private void validateNoDuplicateCustomer(CreateCustomerRequest request) {
        if (request.getNationalId() != null && customerRepository.existsByNationalId(request.getNationalId())) {
            throw new ValidationException("Customer with this national ID already exists");
        }
        
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new ValidationException("Customer with this phone number already exists");
        }
        
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Customer with this email already exists");
        }
    }

    /**
     * Create customer entity from request
     */
    private Customer createCustomerFromRequest(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.valueOf(request.getCustomerType()));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setLegalName(request.getLegalName());
        customer.setNationalId(request.getNationalId());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setGender(request.getGender());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setPrimaryBranchId(Long.parseLong(request.getPrimaryBranchId()));
        customer.setAddress(request.getAddress());
        customer.setOccupation(request.getOccupation());
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setKycStatus("PENDING");
        customer.setKycDocuments(request.getKycDocuments());
        customer.setCustomData(request.getCustomData());
        customer.setActive(true);
        Long currentUserId = BranchContextHolder.getCurrentUserId();
        customer.setCreatedBy(currentUserId != null ? currentUserId.toString() : "system");
        return customer;
    }

    /**
     * Encrypt sensitive customer data
     */
    private void encryptSensitiveCustomerData(Customer customer) {
        // Encrypt national ID if present (reusing existing encryption)
        if (customer.getNationalId() != null) {
            customer.setNationalId(encryptionService.encryptData(customer.getNationalId()));
        }
    }

    /**
     * Decrypt sensitive customer data for display
     */
    private void decryptSensitiveCustomerData(Customer customer) {
        // Decrypt national ID for display (reusing existing encryption)
        if (customer.getNationalId() != null) {
            try {
                customer.setNationalId(encryptionService.decryptData(customer.getNationalId()));
            } catch (Exception e) {
                log.warn("Failed to decrypt national ID for customer: {}", customer.getId());
            }
        }
    }

    /**
     * Validate customer access based on branch context
     */
    private void validateCustomerAccess(Customer customer) {
        if (!BranchContextHolder.canCurrentUserAccessBranch(customer.getPrimaryBranchId())) {
            throw new BusinessException("Access denied to customer", "CUSTOMER_ACCESS_DENIED");
        }
    }

    /**
     * Find customer by ID with validation
     */
    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("Customer not found", "CUSTOMER_NOT_FOUND"));
    }

    /**
     * Get effective branch ID based on user context
     */
    private String getEffectiveBranchId(String requestedBranchId) {
        // Institution admins can search across all branches
        if (BranchContextHolder.isCurrentUserInstitutionAdmin()) {
            return requestedBranchId; // Use requested branch or null for all
        }
        
        // Branch users can only search their own branch
        Long currentBranchId = BranchContextHolder.getCurrentBranchId();
        return currentBranchId != null ? currentBranchId.toString() : null;
    }

    /**
     * Execute customer search using repository
     */
    private Page<Customer> executeCustomerSearch(CustomerSearchRequest searchRequest, String branchId, Pageable pageable) {
        Long branchIdLong = branchId != null ? Long.parseLong(branchId) : null;
        
        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            if (branchIdLong != null) {
                return customerRepository.searchActiveCustomersByBranch(branchIdLong, searchRequest.getSearchTerm(), pageable);
            } else {
                return customerRepository.searchActiveCustomers(searchRequest.getSearchTerm(), pageable);
            }
        } else {
            if (branchIdLong != null) {
                return customerRepository.findByPrimaryBranchId(branchIdLong, pageable);
            } else {
                return customerRepository.findAll(pageable);
            }
        }
    }

    /**
     * Convert page to DTO response
     */
    private PaginatedResponse<CustomerDto> convertToCustomerDtoPage(Page<Customer> customerPage) {
        return PaginatedResponse.of(
                customerPage.getContent().stream()
                        .map(customer -> {
                            decryptSensitiveCustomerData(customer);
                            CustomerDto dto = customerMapper.toDto(customer);
                            enrichCustomerDto(dto);
                            return dto;
                        })
                        .toList(),
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements()
        );
    }

    /**
     * Enrich customer DTO with computed fields
     */
    private void enrichCustomerDto(CustomerDto dto) {
        // Build full name
        if (dto.getFirstName() != null && dto.getLastName() != null) {
            dto.setFullName(dto.getFirstName() + " " + dto.getLastName());
        } else if (dto.getLegalName() != null) {
            dto.setFullName(dto.getLegalName());
        }
        
        // Add branch name (could be cached lookup)
        // dto.setBranchName(getBranchName(dto.getPrimaryBranchId()));
        
        // Add account count (could be cached)
        // dto.setAccountCount(getCustomerAccountCount(dto.getId()));
    }

    /**
     * Validate can deactivate customer
     */
    private void validateCanDeactivateCustomer(Customer customer) {
        // Check for active accounts, loans, etc.
        // This would use existing repositories to check dependencies
        log.debug("Validating customer can be deactivated: {}", customer.getId());
    }

    /**
     * Update customer KYC information
     */
    private void updateCustomerKyc(Customer customer, String kycStatus, Object documents) {
        customer.setKycStatus(kycStatus);
        if (documents != null) {
            customer.setKycDocuments((java.util.Map<String, Object>) documents);
        }
    }

    /**
     * Update customer fields from request
     */
    private void updateCustomerFields(Customer customer, UpdateCustomerRequest request) {
        if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (request.getLastName() != null) customer.setLastName(request.getLastName());
        if (request.getLegalName() != null) customer.setLegalName(request.getLegalName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getOccupation() != null) customer.setOccupation(request.getOccupation());
        if (request.getMonthlyIncome() != null) customer.setMonthlyIncome(request.getMonthlyIncome());
        if (request.getCustomData() != null) customer.setCustomData(request.getCustomData());
    }

    /**
     * Validate update request
     */
    private void validateUpdateCustomerRequest(UpdateCustomerRequest request) {
        if (request.getPhone() != null) {
            phoneNumberValidator.validate(request.getPhone());
        }
    }

    /**
     * Calculate customer risk score
     */
    private BigDecimal calculateCustomerRiskScore(String customerId) {
        // Implement risk scoring algorithm based on business rules
        // This is a placeholder - would involve complex calculations
        return new BigDecimal("50.00"); // Medium risk
    }

    /**
     * Async post-creation operations
     */
    @Async
    private CompletableFuture<Void> performPostCreationOperationsAsync(Long customerId) {
        log.info("Performing post-creation operations for customer: {}", customerId);
        
        try {
            // Calculate initial risk score
            calculateRiskScoreAsync(customerId);
            
            // Send welcome communication (could use email/SMS services)
            // sendWelcomeMessage(customerId);
            
            log.info("Post-creation operations completed for customer: {}", customerId);
            
        } catch (Exception e) {
            log.error("Failed post-creation operations for customer: {}", customerId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Mask sensitive data for logging (privacy protection)
     */
    private String maskSensitiveData(String data) {
        if (data == null || data.length() < 4) {
            return "****";
        }
        return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }

    // Additional interface methods implementation

    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = "customers", key = "#customerId")
    public void activateCustomer(Long customerId) {
        log.info("Activating customer: {}", customerId);
        
        Customer customer = findCustomerById(customerId);
        validateCustomerAccess(customer);
        
        customer.setActive(true);
        customerRepository.save(customer);
        
        log.info("Customer activated successfully: {}", customerId);
    }

    /**
     * Calculate customer risk score based on business rules
     * TODO: Implement actual risk calculation logic
     */
    private BigDecimal calculateCustomerRiskScore(Long customerId) {
        // Placeholder implementation - returns a default risk score
        // In a real implementation, this would analyze:
        // - Transaction patterns
        // - Account balances
        // - KYC status
        // - Geographic risk factors
        // - Industry risk factors, etc.
        
        log.debug("Calculating risk score for customer: {} (placeholder implementation)", customerId);
        return BigDecimal.valueOf(50.0); // Default medium risk score
    }
}
