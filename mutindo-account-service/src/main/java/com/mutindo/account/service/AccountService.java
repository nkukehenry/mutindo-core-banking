package com.mutindo.account.service;

import com.mutindo.account.dto.*;
import com.mutindo.account.mapper.AccountMapper;
import com.mutindo.common.context.BranchContextHolder;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.common.enums.AccountStatus;
import com.mutindo.customer.service.ICustomerService;
import com.mutindo.entities.Account;
import com.mutindo.entities.Product;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.AccountRepository;
import com.mutindo.repositories.BranchRepository;
import com.mutindo.repositories.ProductRepository;
import com.mutindo.validation.validator.AccountNumberValidator;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Full Account service implementation with comprehensive business logic
 * Reuses existing infrastructure: Mappers, Repositories, Validation, Caching, Logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountNumberValidator accountNumberValidator;
    private final ICustomerService customerService;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    
    // Specialized services for better separation of concerns
    private final AccountNotificationService accountNotificationService;
    private final AccountLimitService accountLimitService;
    private final AccountCustomFieldService accountCustomFieldService;

    @Override
    @Transactional
    @AuditLog(action = "CREATE_ACCOUNT", entity = "Account")
    @PerformanceLog
    @CacheEvict(value = {"accounts", "customerAccounts", "branchAccounts"}, allEntries = true)
    public AccountDto createAccount(CreateAccountRequest request) {
        log.info("Creating new account for customer: {} with product: {}", request.getCustomerId(), request.getProductCode());

        try {
            // Validate request
            validateCreateAccountRequest(request);
            
            // Validate customer exists and user has access
            Long customerId = Long.parseLong(request.getCustomerId());
            validateCustomerAccess(customerId);
            
            // Validate product exists and is active
            Product product = validateAndGetProduct(request.getProductCode());
            
            // Validate branch access
            Long branchId = Long.parseLong(request.getBranchId());
            validateBranchAccess(branchId);
            
            // Generate unique account number
            String accountNumber = generateUniqueAccountNumber(request.getProductCode(), request.getBranchId());
            
            // Create account entity
            Account account = buildAccountEntity(request, customerId, branchId, product, accountNumber);
            
            // Save account
            Account savedAccount = accountRepository.save(account);
            
            // Perform async setup operations
            performAccountSetupAsync(savedAccount.getId());
            
            // Convert to DTO and enrich
            AccountDto accountDto = accountMapper.toDto(savedAccount);
            enrichAccountDto(accountDto);
            
            log.info("Successfully created account: {} for customer: {}", accountNumber, customerId);
            return accountDto;
            
        } catch (Exception e) {
            log.error("Failed to create account for customer: {}", request.getCustomerId(), e);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "accounts", key = "#accountId")
    @PerformanceLog
    public Optional<AccountDto> getAccountById(Long accountId) {
        log.debug("Getting account by ID: {}", accountId);
        
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            
            if (accountOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Account account = accountOpt.get();
            
            // Validate branch access
            validateAccountAccess(account);
            
            // Convert to DTO and enrich
            AccountDto accountDto = accountMapper.toDto(account);
            enrichAccountDto(accountDto);
            
            return Optional.of(accountDto);
            
        } catch (Exception e) {
            log.error("Failed to get account by ID: {}", accountId, e);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "accounts", key = "#accountNumber")
    @PerformanceLog
    public Optional<AccountDto> getAccountByNumber(String accountNumber) {
        log.debug("Getting account by number: {}", accountNumber);
        
        try {
            // Validate account number format
            accountNumberValidator.validate(accountNumber);
            
            Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            
            if (accountOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Account account = accountOpt.get();
            
            // Validate branch access
            validateAccountAccess(account);
            
            // Convert to DTO and enrich
            AccountDto accountDto = accountMapper.toDto(account);
            enrichAccountDto(accountDto);
            
            return Optional.of(accountDto);
            
        } catch (Exception e) {
            log.error("Failed to get account by number: {}", accountNumber, e);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "customerAccounts", key = "#customerId + ':' + #pageable.pageNumber")
    @PerformanceLog
    public PaginatedResponse<AccountDto> getCustomerAccounts(Long customerId, Pageable pageable) {
        log.debug("Getting accounts for customer: {}", customerId);
        
        try {
            // Validate customer access
            validateCustomerAccess(customerId);
            
            Page<Account> accountPage = accountRepository.findByCustomerId(customerId, pageable);
            
            return convertToAccountDtoPage(accountPage);
            
        } catch (Exception e) {
            log.error("Failed to get customer accounts: {}", customerId, e);
            throw e;
        }
    }

    @Override
    @PerformanceLog
    public PaginatedResponse<AccountDto> searchAccounts(AccountSearchRequest searchRequest, Pageable pageable) {
        log.debug("Searching accounts with criteria: {}", searchRequest.getSearchTerm());
        
        try {
            // Apply branch filtering based on user context
            String effectiveBranchId = getEffectiveBranchId(searchRequest.getBranchId());
            
            Page<Account> accountPage;
            if (effectiveBranchId != null) {
                // Search within specific branch
                Long branchIdLong = Long.parseLong(effectiveBranchId);
                accountPage = accountRepository.searchAccountsByBranch(branchIdLong, searchRequest.getSearchTerm(), pageable);
            } else {
                // Search across all accessible branches
                accountPage = accountRepository.searchAccounts(searchRequest.getSearchTerm(), pageable);
            }
            
            return convertToAccountDtoPage(accountPage);
            
        } catch (Exception e) {
            log.error("Failed to search accounts", e);
            throw e;
        }
    }

    @Cacheable(value = "branchAccounts", key = "#branchId + ':' + #pageable.pageNumber")
    @PerformanceLog
    public PaginatedResponse<AccountDto> getAccountsByBranch(String branchId, Pageable pageable) {
        log.debug("Getting accounts for branch: {}", branchId);
        
        try {
            // Validate branch access
            Long branchIdLong = Long.parseLong(branchId);
            validateBranchAccess(branchIdLong);
            
            Page<Account> accountPage = accountRepository.findByBranchId(branchIdLong, pageable);
            
            return convertToAccountDtoPage(accountPage);
            
        } catch (Exception e) {
            log.error("Failed to get branch accounts: {}", branchId, e);
            throw e;
        }
    }

    @Override
    public AccountBalanceDto getAccountBalance(Long accountId) {
        log.debug("Getting balance for account: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", "ACCOUNT_NOT_FOUND"));
        
        return AccountBalanceDto.builder()
                .accountId(accountId.toString())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .currency(account.getCurrency())
                .build();
    }

    @Override
    public void updateAccountBalance(Long accountId, BigDecimal newBalance, BigDecimal availableBalance) {
        log.debug("Updating balance for account: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", "ACCOUNT_NOT_FOUND"));
        
        account.setBalance(newBalance);
        account.setAvailableBalance(availableBalance);
        accountRepository.save(account);
    }

    @Override
    public void closeAccount(Long accountId, String reason) {
        log.info("Closing account: {} - Reason: {}", accountId, reason);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", "ACCOUNT_NOT_FOUND"));
        
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    @Override
    public void freezeAccount(Long accountId, String reason) {
        log.info("Freezing account: {} - Reason: {}", accountId, reason);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", "ACCOUNT_NOT_FOUND"));
        
        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
    }

    @Override
    public void unfreezeAccount(Long accountId) {
        log.info("Unfreezing account: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", "ACCOUNT_NOT_FOUND"));
        
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Override
    public String generateAccountNumber(String productCode, String branchId) {
        // Simple account number generation
        String timestamp = String.valueOf(System.currentTimeMillis());
        return productCode + branchId + timestamp.substring(timestamp.length() - 6);
    }

    @Override
    @Async
    public CompletableFuture<Void> performAccountSetupAsync(Long accountId) {
        log.info("Performing account setup asynchronously for: {}", accountId);
        
        try {
            // Get account details
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new BusinessException("Account not found", "ACCOUNT_NOT_FOUND"));
            
            // Perform setup operations using specialized services
            accountNotificationService.setupAccountNotifications(account);
            accountLimitService.setupAccountLimits(account);
            accountCustomFieldService.setupAccountCustomFields(account);
            
            log.info("Account setup completed for: {}", accountId);
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("Failed to setup account asynchronously: {}", accountId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public boolean isAccountActive(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> AccountStatus.ACTIVE.equals(account.getStatus()))
                .orElse(false);
    }

    // =================================================================================
    // PRIVATE HELPER METHODS - Reusable business logic
    // =================================================================================

    /**
     * Validate create account request
     */
    private void validateCreateAccountRequest(CreateAccountRequest request) {
        if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
            throw new ValidationException("Customer ID is required");
        }
        
        if (request.getProductCode() == null || request.getProductCode().trim().isEmpty()) {
            throw new ValidationException("Product code is required");
        }
        
        if (request.getBranchId() == null || request.getBranchId().trim().isEmpty()) {
            throw new ValidationException("Branch ID is required");
        }
        
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            throw new ValidationException("Currency is required");
        }
        
        if (request.getInitialDeposit() != null && request.getInitialDeposit().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Initial deposit cannot be negative");
        }
    }

    /**
     * Validate customer access and existence
     */
    private void validateCustomerAccess(Long customerId) {
        Optional<com.mutindo.customer.dto.CustomerDto> customerOpt = customerService.getCustomerById(customerId);
        if (customerOpt.isEmpty()) {
            throw new BusinessException("Customer not found", "CUSTOMER_NOT_FOUND");
        }
        
        // Additional access validation can be added here
    }

    /**
     * Validate and get product
     */
    private Product validateAndGetProduct(String productCode) {
        return productRepository.findByCode(productCode)
                .orElseThrow(() -> new BusinessException("Product not found: " + productCode, "PRODUCT_NOT_FOUND"));
    }

    /**
     * Validate branch access
     */
    private void validateBranchAccess(Long branchId) {
        if (!BranchContextHolder.canCurrentUserAccessBranch(branchId)) {
            throw new BusinessException("Access denied to branch: " + branchId, "BRANCH_ACCESS_DENIED");
        }
        
        if (!branchRepository.existsById(branchId)) {
            throw new BusinessException("Branch not found: " + branchId, "BRANCH_NOT_FOUND");
        }
    }

    /**
     * Validate account access
     */
    private void validateAccountAccess(Account account) {
        if (!BranchContextHolder.canCurrentUserAccessBranch(account.getBranchId())) {
            throw new BusinessException("Access denied to account", "ACCOUNT_ACCESS_DENIED");
        }
    }

    /**
     * Generate unique account number
     */
    private String generateUniqueAccountNumber(String productCode, String branchId) {
        String accountNumber;
        int attempts = 0;
        int maxAttempts = 10;
        
        do {
            accountNumber = generateAccountNumber(productCode, branchId);
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new BusinessException("Unable to generate unique account number", "ACCOUNT_NUMBER_GENERATION_FAILED");
            }
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }

    /**
     * Build account entity from request
     */
    private Account buildAccountEntity(CreateAccountRequest request, Long customerId, Long branchId, Product product, String accountNumber) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCustomerId(customerId);
        account.setBranchId(branchId);
        account.setProductId(product.getId());
        account.setCurrency(request.getCurrency());
        account.setBalance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO);
        account.setAvailableBalance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setOpenedAt(LocalDateTime.now());
        account.setDailyWithdrawalLimit(request.getDailyWithdrawalLimit() != null ? request.getDailyWithdrawalLimit() : product.getMaxBalance());
        account.setMinimumBalance(request.getMinimumBalance() != null ? request.getMinimumBalance() : product.getMinBalance());
        account.setOverdraftLimit(request.getOverdraftLimit() != null ? request.getOverdraftLimit() : BigDecimal.ZERO);
        account.setCustomData(request.getCustomData());
        
        // Set audit fields
        Long currentUserId = BranchContextHolder.getCurrentUserId();
        account.setCreatedBy(currentUserId != null ? currentUserId.toString() : "system");
        
        return account;
    }

    /**
     * Enrich account DTO with computed fields
     */
    private void enrichAccountDto(AccountDto accountDto) {
        try {
            // Get customer name
            if (accountDto.getCustomerId() != null) {
                Long customerId = Long.parseLong(accountDto.getCustomerId());
                customerService.getCustomerById(customerId)
                        .ifPresent(customer -> accountDto.setCustomerName(customer.getFullName()));
            }
            
            // Get branch name
            if (accountDto.getBranchId() != null) {
                Long branchId = Long.parseLong(accountDto.getBranchId());
                branchRepository.findById(branchId)
                        .ifPresent(branch -> accountDto.setBranchName(branch.getName()));
            }
            
            // Get product name
            if (accountDto.getProductCode() != null) {
                productRepository.findByCode(accountDto.getProductCode())
                        .ifPresent(product -> accountDto.setProductName(product.getName()));
            }
            
            // TODO: Add transaction statistics
            // accountDto.setTotalTransactionsToday(getTotalTransactionsToday(accountDto.getId()));
            // accountDto.setTransactionCountToday(getTransactionCountToday(accountDto.getId()));
            // accountDto.setLastTransactionDate(getLastTransactionDate(accountDto.getId()));
            
        } catch (Exception e) {
            log.warn("Failed to enrich account DTO: {}", accountDto.getId(), e);
        }
    }

    /**
     * Convert Page<Account> to PaginatedResponse<AccountDto>
     */
    private PaginatedResponse<AccountDto> convertToAccountDtoPage(Page<Account> accountPage) {
        List<AccountDto> accountDtos = accountPage.getContent().stream()
                .map(account -> {
                    AccountDto dto = accountMapper.toDto(account);
                    enrichAccountDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
        
        return PaginatedResponse.<AccountDto>builder()
                .content(accountDtos)
                .totalElements(accountPage.getTotalElements())
                .totalPages(accountPage.getTotalPages())
                .size(accountPage.getSize())
                .page(accountPage.getNumber())
                .first(accountPage.isFirst())
                .last(accountPage.isLast())
                .build();
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

}
