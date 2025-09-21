package com.mutindo.account.service;

import com.mutindo.account.dto.AccountDto;
import com.mutindo.account.dto.CreateAccountRequest;
import com.mutindo.account.dto.AccountSearchRequest;
import com.mutindo.account.dto.AccountBalanceDto;
import com.mutindo.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Account service interface for polymorphic account operations
 * Follows our established pattern of interface-driven design
 */
public interface IAccountService {
    
    /**
     * Create new account with product validation
     * @param request Account creation request
     * @return Created account information
     */
    AccountDto createAccount(CreateAccountRequest request);
    
    /**
     * Get account by ID with branch access validation
     * @param accountId Account ID
     * @return Account information if authorized
     */
    Optional<AccountDto> getAccountById(Long accountId);
    
    /**
     * Get account by account number with validation
     * @param accountNumber Account number
     * @return Account information if authorized
     */
    Optional<AccountDto> getAccountByNumber(String accountNumber);
    
    /**
     * Get customer accounts with branch filtering
     * @param customerId Customer ID
     * @param pageable Pagination parameters
     * @return Customer's accounts
     */
    PaginatedResponse<AccountDto> getCustomerAccounts(Long customerId, Pageable pageable);
    
    /**
     * Search accounts with pagination and branch filtering
     * @param searchRequest Search criteria
     * @param pageable Pagination parameters
     * @return Paginated account results
     */
    PaginatedResponse<AccountDto> searchAccounts(AccountSearchRequest searchRequest, Pageable pageable);
    
    /**
     * Get account balance with real-time calculation
     * @param accountId Account ID
     * @return Current balance information
     */
    AccountBalanceDto getAccountBalance(Long accountId);
    
    /**
     * Update account balance (internal use by transaction service)
     * @param accountId Account ID
     * @param newBalance New balance amount
     * @param availableBalance New available balance
     */
    void updateAccountBalance(Long accountId, BigDecimal newBalance, BigDecimal availableBalance);
    
    /**
     * Close account with validation
     * @param accountId Account ID
     * @param reason Closure reason
     */
    void closeAccount(Long accountId, String reason);
    
    /**
     * Freeze account (temporary suspension)
     * @param accountId Account ID
     * @param reason Freeze reason
     */
    void freezeAccount(Long accountId, String reason);
    
    /**
     * Unfreeze account
     * @param accountId Account ID
     */
    void unfreezeAccount(Long accountId);
    
    /**
     * Check if account exists and is active
     * @param accountNumber Account number
     * @return true if account exists and is active
     */
    boolean isAccountActive(String accountNumber);
    
    /**
     * Generate unique account number
     * @param productCode Product code
     * @param branchId Branch ID
     * @return Generated account number
     */
    String generateAccountNumber(String productCode, String branchId);
    
    /**
     * Async account setup operations
     * @param accountId Account ID
     * @return Future completion
     */
    CompletableFuture<Void> performAccountSetupAsync(Long accountId);
}
