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
            
            // Perform setup operations
            setupAccountNotifications(account);
            setupAccountLimits(account);
            setupAccountCustomFields(account);
            
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
                .pageNumber(accountPage.getNumber())
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

    /**
     * Setup account notifications - comprehensive notification configuration
     */
    private void setupAccountNotifications(Account account) {
        log.debug("Setting up notifications for account: {}", account.getAccountNumber());
        
        try {
            // Get product configuration for notification settings
            Product product = productRepository.findById(account.getProductId()).orElse(null);
            if (product == null) {
                log.warn("Product not found for account: {}", account.getAccountNumber());
                return;
            }
            
            // Setup default notification preferences based on product type
            setupDefaultNotificationPreferences(account, product);
            
            // Setup transaction notifications
            setupTransactionNotifications(account, product);
            
            // Setup balance alerts
            setupBalanceAlerts(account, product);
            
            // Setup statement notifications
            setupStatementNotifications(account, product);
            
            log.info("Notification setup completed for account: {}", account.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Failed to setup notifications for account: {}", account.getAccountNumber(), e);
            // Don't throw exception as this is async operation
        }
    }

    /**
     * Setup account limits - comprehensive limit configuration
     */
    private void setupAccountLimits(Account account) {
        log.debug("Setting up limits for account: {}", account.getAccountNumber());
        
        try {
            // Get product configuration for limit settings
            Product product = productRepository.findById(account.getProductId()).orElse(null);
            if (product == null) {
                log.warn("Product not found for account: {}", account.getAccountNumber());
                return;
            }
            
            // Setup transaction limits
            setupTransactionLimits(account, product);
            
            // Setup balance limits
            setupBalanceLimits(account, product);
            
            // Setup time-based limits
            setupTimeBasedLimits(account, product);
            
            // Setup channel-specific limits
            setupChannelLimits(account, product);
            
            log.info("Limit setup completed for account: {}", account.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Failed to setup limits for account: {}", account.getAccountNumber(), e);
            // Don't throw exception as this is async operation
        }
    }

    /**
     * Setup account custom fields - dynamic field configuration
     */
    private void setupAccountCustomFields(Account account) {
        log.debug("Setting up custom fields for account: {}", account.getAccountNumber());
        
        try {
            // Get product configuration for custom fields
            Product product = productRepository.findById(account.getProductId()).orElse(null);
            if (product == null) {
                log.warn("Product not found for account: {}", account.getAccountNumber());
                return;
            }
            
            // Setup product-specific custom fields
            setupProductCustomFields(account, product);
            
            // Setup account-specific custom fields
            setupAccountSpecificFields(account);
            
            // Setup regulatory custom fields
            setupRegulatoryFields(account, product);
            
            // Setup business-specific custom fields
            setupBusinessFields(account, product);
            
            log.info("Custom fields setup completed for account: {}", account.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Failed to setup custom fields for account: {}", account.getAccountNumber(), e);
            // Don't throw exception as this is async operation
        }
    }

    // =================================================================================
    // NOTIFICATION SETUP HELPER METHODS
    // =================================================================================

    private void setupDefaultNotificationPreferences(Account account, Product product) {
        // Setup default notification preferences based on product type
        Map<String, Object> notificationPrefs = new java.util.HashMap<>();
        
        switch (product.getProductType().toUpperCase()) {
            case "SAVINGS":
                notificationPrefs.put("lowBalanceAlert", true);
                notificationPrefs.put("transactionAlert", true);
                notificationPrefs.put("statementAlert", true);
                notificationPrefs.put("interestCreditAlert", true);
                break;
            case "CURRENT":
                notificationPrefs.put("lowBalanceAlert", true);
                notificationPrefs.put("transactionAlert", false); // Too frequent for current accounts
                notificationPrefs.put("statementAlert", true);
                notificationPrefs.put("overdraftAlert", true);
                break;
            case "LOAN":
                notificationPrefs.put("paymentDueAlert", true);
                notificationPrefs.put("paymentOverdueAlert", true);
                notificationPrefs.put("paymentReceivedAlert", true);
                notificationPrefs.put("loanDisbursementAlert", true);
                break;
            default:
                notificationPrefs.put("transactionAlert", true);
                notificationPrefs.put("statementAlert", true);
        }
        
        // Store in account custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("notificationPreferences", notificationPrefs);
        account.setCustomData(customData);
        
        log.debug("Default notification preferences set for account: {}", account.getAccountNumber());
    }

    private void setupTransactionNotifications(Account account, Product product) {
        // Setup transaction notification thresholds
        Map<String, Object> transactionAlerts = new java.util.HashMap<>();
        
        // Set thresholds based on product type and limits
        BigDecimal dailyLimit = account.getDailyWithdrawalLimit();
        if (dailyLimit != null) {
            transactionAlerts.put("largeTransactionThreshold", dailyLimit.multiply(new BigDecimal("0.8")));
            transactionAlerts.put("dailyLimitWarningThreshold", dailyLimit.multiply(new BigDecimal("0.9")));
        }
        
        // Set notification channels
        transactionAlerts.put("smsEnabled", true);
        transactionAlerts.put("emailEnabled", true);
        transactionAlerts.put("pushEnabled", false); // Can be enabled later
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("transactionAlerts", transactionAlerts);
        account.setCustomData(customData);
        
        log.debug("Transaction notifications configured for account: {}", account.getAccountNumber());
    }

    private void setupBalanceAlerts(Account account, Product product) {
        // Setup balance alert thresholds
        Map<String, Object> balanceAlerts = new java.util.HashMap<>();
        
        BigDecimal minBalance = account.getMinimumBalance();
        if (minBalance != null) {
            balanceAlerts.put("lowBalanceThreshold", minBalance.multiply(new BigDecimal("1.1")));
            balanceAlerts.put("criticalBalanceThreshold", minBalance);
        }
        
        // Set alert frequencies
        balanceAlerts.put("lowBalanceFrequency", "IMMEDIATE");
        balanceAlerts.put("criticalBalanceFrequency", "IMMEDIATE");
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("balanceAlerts", balanceAlerts);
        account.setCustomData(customData);
        
        log.debug("Balance alerts configured for account: {}", account.getAccountNumber());
    }

    private void setupStatementNotifications(Account account, Product product) {
        // Setup statement notification preferences
        Map<String, Object> statementAlerts = new java.util.HashMap<>();
        
        // Set statement frequency based on product type
        switch (product.getProductType().toUpperCase()) {
            case "SAVINGS":
                statementAlerts.put("frequency", "MONTHLY");
                break;
            case "CURRENT":
                statementAlerts.put("frequency", "MONTHLY");
                break;
            case "LOAN":
                statementAlerts.put("frequency", "MONTHLY");
                break;
            default:
                statementAlerts.put("frequency", "QUARTERLY");
        }
        
        statementAlerts.put("emailEnabled", true);
        statementAlerts.put("smsEnabled", false);
        statementAlerts.put("paperEnabled", false);
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("statementAlerts", statementAlerts);
        account.setCustomData(customData);
        
        log.debug("Statement notifications configured for account: {}", account.getAccountNumber());
    }

    // =================================================================================
    // LIMIT SETUP HELPER METHODS
    // =================================================================================

    private void setupTransactionLimits(Account account, Product product) {
        // Setup transaction limits based on product configuration
        Map<String, Object> transactionLimits = new java.util.HashMap<>();
        
        // Daily limits
        if (product.getDailyWithdrawalLimit() != null) {
            transactionLimits.put("dailyWithdrawalLimit", product.getDailyWithdrawalLimit());
        }
        
        // Monthly limits
        if (product.getMonthlyWithdrawalLimit() != null) {
            transactionLimits.put("monthlyWithdrawalLimit", product.getMonthlyWithdrawalLimit());
        }
        
        // Single transaction limits
        if (product.getMaxTransactionAmount() != null) {
            transactionLimits.put("maxSingleTransaction", product.getMaxTransactionAmount());
        }
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("transactionLimits", transactionLimits);
        account.setCustomData(customData);
        
        log.debug("Transaction limits configured for account: {}", account.getAccountNumber());
    }

    private void setupBalanceLimits(Account account, Product product) {
        // Setup balance limits
        Map<String, Object> balanceLimits = new java.util.HashMap<>();
        
        // Minimum balance
        if (product.getMinBalance() != null) {
            balanceLimits.put("minimumBalance", product.getMinBalance());
        }
        
        // Maximum balance
        if (product.getMaxBalance() != null) {
            balanceLimits.put("maximumBalance", product.getMaxBalance());
        }
        
        // Overdraft limits
        if (product.getAllowsOverdraft()) {
            balanceLimits.put("overdraftLimit", account.getOverdraftLimit());
            balanceLimits.put("overdraftEnabled", true);
        } else {
            balanceLimits.put("overdraftEnabled", false);
        }
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("balanceLimits", balanceLimits);
        account.setCustomData(customData);
        
        log.debug("Balance limits configured for account: {}", account.getAccountNumber());
    }

    private void setupTimeBasedLimits(Account account, Product product) {
        // Setup time-based transaction limits
        Map<String, Object> timeLimits = new java.util.HashMap<>();
        
        // Business hours restrictions
        timeLimits.put("businessHoursOnly", false);
        timeLimits.put("businessHoursStart", "08:00");
        timeLimits.put("businessHoursEnd", "17:00");
        
        // Weekend restrictions
        timeLimits.put("weekendTransactionsAllowed", true);
        timeLimits.put("weekendLimitMultiplier", new BigDecimal("0.5"));
        
        // Holiday restrictions
        timeLimits.put("holidayTransactionsAllowed", true);
        timeLimits.put("holidayLimitMultiplier", new BigDecimal("0.3"));
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("timeLimits", timeLimits);
        account.setCustomData(customData);
        
        log.debug("Time-based limits configured for account: {}", account.getAccountNumber());
    }

    private void setupChannelLimits(Account account, Product product) {
        // Setup channel-specific limits
        Map<String, Object> channelLimits = new java.util.HashMap<>();
        
        // ATM limits
        Map<String, Object> atmLimits = new java.util.HashMap<>();
        atmLimits.put("dailyLimit", account.getDailyWithdrawalLimit() != null ? 
            account.getDailyWithdrawalLimit().multiply(new BigDecimal("0.3")) : new BigDecimal("100000"));
        atmLimits.put("singleTransactionLimit", new BigDecimal("50000"));
        channelLimits.put("atm", atmLimits);
        
        // Mobile/Online limits
        Map<String, Object> mobileLimits = new java.util.HashMap<>();
        mobileLimits.put("dailyLimit", account.getDailyWithdrawalLimit() != null ? 
            account.getDailyWithdrawalLimit().multiply(new BigDecimal("0.7")) : new BigDecimal("200000"));
        mobileLimits.put("singleTransactionLimit", new BigDecimal("100000"));
        channelLimits.put("mobile", mobileLimits);
        
        // Branch limits
        Map<String, Object> branchLimits = new java.util.HashMap<>();
        branchLimits.put("dailyLimit", account.getDailyWithdrawalLimit());
        branchLimits.put("singleTransactionLimit", account.getDailyWithdrawalLimit());
        channelLimits.put("branch", branchLimits);
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("channelLimits", channelLimits);
        account.setCustomData(customData);
        
        log.debug("Channel limits configured for account: {}", account.getAccountNumber());
    }

    // =================================================================================
    // CUSTOM FIELDS SETUP HELPER METHODS
    // =================================================================================

    private void setupProductCustomFields(Account account, Product product) {
        // Setup product-specific custom fields
        Map<String, Object> productFields = new java.util.HashMap<>();
        
        // Product-specific fields based on type
        switch (product.getProductType().toUpperCase()) {
            case "SAVINGS":
                productFields.put("interestCalculationMethod", "DAILY_BALANCE");
                productFields.put("interestPaymentFrequency", "MONTHLY");
                productFields.put("minimumDepositAmount", product.getMinBalance());
                break;
            case "CURRENT":
                productFields.put("checkBookEnabled", true);
                productFields.put("debitCardEnabled", true);
                productFields.put("onlineBankingEnabled", true);
                break;
            case "LOAN":
                productFields.put("repaymentMethod", product.getRepaymentFrequency());
                productFields.put("interestCalculationMethod", product.getInterestCalculationMethod());
                productFields.put("guarantorRequired", product.getRequiresGuarantor());
                break;
        }
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("productFields", productFields);
        account.setCustomData(customData);
        
        log.debug("Product custom fields configured for account: {}", account.getAccountNumber());
    }

    private void setupAccountSpecificFields(Account account) {
        // Setup account-specific custom fields
        Map<String, Object> accountFields = new java.util.HashMap<>();
        
        // Account opening information
        accountFields.put("openingChannel", "BRANCH");
        accountFields.put("openingOfficer", account.getCreatedBy());
        accountFields.put("openingDate", account.getOpenedAt());
        
        // Account status tracking
        accountFields.put("lastActivityDate", account.getOpenedAt());
        accountFields.put("transactionCount", 0);
        accountFields.put("averageBalance", account.getBalance());
        
        // Risk assessment fields
        accountFields.put("riskLevel", "LOW");
        accountFields.put("kycStatus", "COMPLETED");
        accountFields.put("amlStatus", "CLEAR");
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("accountFields", accountFields);
        account.setCustomData(customData);
        
        log.debug("Account-specific fields configured for account: {}", account.getAccountNumber());
    }

    private void setupRegulatoryFields(Account account, Product product) {
        // Setup regulatory compliance fields
        Map<String, Object> regulatoryFields = new java.util.HashMap<>();
        
        // AML/KYC fields
        regulatoryFields.put("amlRiskCategory", "STANDARD");
        regulatoryFields.put("kycLevel", "ENHANCED");
        regulatoryFields.put("sourceOfFunds", "SALARY");
        
        // Regulatory reporting
        regulatoryFields.put("reportingRequired", true);
        regulatoryFields.put("reportingFrequency", "MONTHLY");
        regulatoryFields.put("regulatoryCategory", product.getProductType());
        
        // Compliance flags
        regulatoryFields.put("sanctionsScreening", true);
        regulatoryFields.put("pepScreening", true);
        regulatoryFields.put("transactionMonitoring", true);
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("regulatoryFields", regulatoryFields);
        account.setCustomData(customData);
        
        log.debug("Regulatory fields configured for account: {}", account.getAccountNumber());
    }

    private void setupBusinessFields(Account account, Product product) {
        // Setup business-specific custom fields
        Map<String, Object> businessFields = new java.util.HashMap<>();
        
        // Business metrics
        businessFields.put("customerSegment", "RETAIL");
        businessFields.put("accountTier", "STANDARD");
        businessFields.put("relationshipManager", null);
        
        // Service preferences
        businessFields.put("preferredLanguage", "EN");
        businessFields.put("preferredContactMethod", "EMAIL");
        businessFields.put("marketingOptIn", false);
        
        // Business rules
        businessFields.put("autoRenewal", false);
        businessFields.put("feeWaiver", false);
        businessFields.put("priorityService", false);
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("businessFields", businessFields);
        account.setCustomData(customData);
        
        log.debug("Business fields configured for account: {}", account.getAccountNumber());
    }
}
