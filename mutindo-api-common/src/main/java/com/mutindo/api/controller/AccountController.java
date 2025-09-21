package com.mutindo.api.controller;

import com.mutindo.account.dto.*; // Reusing existing account DTOs
import com.mutindo.account.dto.AccountBalanceDto;
import com.mutindo.account.dto.AccountDto;
import com.mutindo.account.dto.AccountSearchRequest;
import com.mutindo.account.dto.CreateAccountRequest;
import com.mutindo.account.service.IAccountService; // Reusing existing service interface
import com.mutindo.common.dto.BaseResponse; // Reusing existing response wrapper
import com.mutindo.common.dto.PaginatedResponse; // Reusing existing pagination
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
 * Account REST API controller
 * Reuses existing account service and response infrastructure
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounts", description = "Account management operations")
public class AccountController {

    private final IAccountService accountService; // Reusing existing service interface

    /**
     * Create new account
     */
    @PostMapping
    @Operation(summary = "Create account", description = "Create a new account for customer")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_CREATE')") // Security check
    @AuditLog // Reusing existing audit logging
    @PerformanceLog // Reusing existing performance logging
    public ResponseEntity<BaseResponse<AccountDto>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Creating account via API - Customer: {} - Product: {}", 
                request.getCustomerId(), request.getProductCode());

        try {
            // Use existing account service
            AccountDto account = accountService.createAccount(request);
            
            log.info("Account created successfully via API: {}", account.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(account, "Account created successfully"));
            
        } catch (Exception e) {
            log.error("Failed to create account via API", e);
            throw e; // Let global exception handler manage the response
        }
    }

    /**
     * Get account by ID
     */
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account", description = "Get account by ID with branch access validation")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_READ')") // Security check
    @PerformanceLog
    public ResponseEntity<BaseResponse<AccountDto>> getAccount(@PathVariable String accountId) {
        log.debug("Getting account via API: {}", accountId);

        try {
            // Convert String ID to Long for service call
            Long accountIdLong = Long.parseLong(accountId);
            Optional<AccountDto> accountOpt = accountService.getAccountById(accountIdLong);
            
            if (accountOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success(accountOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("Account not found"));
            }
            
        } catch (Exception e) {
            log.error("Failed to get account via API: {}", accountId, e);
            throw e;
        }
    }

    /**
     * Get account by account number
     */
    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by number", description = "Get account by account number")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_READ')") // Security check
    @PerformanceLog
    public ResponseEntity<BaseResponse<AccountDto>> getAccountByNumber(@PathVariable String accountNumber) {
        log.debug("Getting account by number via API: {}", accountNumber);

        try {
            // Use existing account service
            Optional<AccountDto> accountOpt = accountService.getAccountByNumber(accountNumber);
            
            if (accountOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success(accountOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("Account not found"));
            }
            
        } catch (Exception e) {
            log.error("Failed to get account by number via API: {}", accountNumber, e);
            throw e;
        }
    }

    /**
     * Get customer accounts
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer accounts", description = "Get all accounts for a customer")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_READ')") // Security check
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<AccountDto>>> getCustomerAccounts(
            @PathVariable String customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting customer accounts via API: {}", customerId);

        try {
            // Convert String ID to Long for service call
            Long customerIdLong = Long.parseLong(customerId);
            PaginatedResponse<AccountDto> accounts = accountService.getCustomerAccounts(customerIdLong, pageable);
            
            log.debug("Found {} accounts for customer via API", accounts.getTotalElements());
            return ResponseEntity.ok(BaseResponse.success(accounts));
            
        } catch (Exception e) {
            log.error("Failed to get customer accounts via API: {}", customerId, e);
            throw e;
        }
    }

    /**
     * Get account balance
     */
    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Get real-time account balance information")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_READ')") // Security check
    @PerformanceLog
    public ResponseEntity<BaseResponse<AccountBalanceDto>> getAccountBalance(@PathVariable String accountId) {
        log.debug("Getting account balance via API: {}", accountId);

        try {
            // Convert String ID to Long for service call
            Long accountIdLong = Long.parseLong(accountId);
            AccountBalanceDto balance = accountService.getAccountBalance(accountIdLong);
            
            return ResponseEntity.ok(BaseResponse.success(balance));
            
        } catch (Exception e) {
            log.error("Failed to get account balance via API: {}", accountId, e);
            throw e;
        }
    }

    /**
     * Search accounts with pagination
     */
    @GetMapping("/search")
    @Operation(summary = "Search accounts", description = "Search accounts with pagination and filtering")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_READ')") // Security check
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<AccountDto>>> searchAccounts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Searching accounts via API - Term: {}", searchTerm);

        try {
            // Build search request (small method)
            AccountSearchRequest searchRequest = buildAccountSearchRequest(
                    searchTerm, customerId, productCode, branchId, status, currency, active);
            
            // Use existing account service
            PaginatedResponse<AccountDto> accounts = accountService.searchAccounts(searchRequest, pageable);
            
            log.debug("Found {} accounts via API", accounts.getTotalElements());
            return ResponseEntity.ok(BaseResponse.success(accounts));
            
        } catch (Exception e) {
            log.error("Failed to search accounts via API", e);
            throw e;
        }
    }

    /**
     * Close account
     */
    @DeleteMapping("/{accountId}")
    @Operation(summary = "Close account", description = "Close account with validation")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_CLOSE')") // Security check
    @AuditLog
    public ResponseEntity<BaseResponse<Void>> closeAccount(
            @PathVariable String accountId,
            @RequestParam String reason) {
        
        log.info("Closing account via API: {} - Reason: {}", accountId, reason);

        try {
            // Convert String ID to Long for service call
            Long accountIdLong = Long.parseLong(accountId);
            accountService.closeAccount(accountIdLong, reason);
            
            log.info("Account closed successfully via API: {}", accountId);
            return ResponseEntity.ok(BaseResponse.success(null, "Account closed successfully"));
            
        } catch (Exception e) {
            log.error("Failed to close account via API: {}", accountId, e);
            throw e;
        }
    }

    /**
     * Freeze account
     */
    @PatchMapping("/{accountId}/freeze")
    @Operation(summary = "Freeze account", description = "Temporarily freeze account")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_FREEZE')") // Security check
    @AuditLog
    public ResponseEntity<BaseResponse<Void>> freezeAccount(
            @PathVariable String accountId,
            @RequestParam String reason) {
        
        log.info("Freezing account via API: {} - Reason: {}", accountId, reason);

        try {
            // Convert String ID to Long for service call
            Long accountIdLong = Long.parseLong(accountId);
            accountService.freezeAccount(accountIdLong, reason);
            
            log.info("Account frozen successfully via API: {}", accountId);
            return ResponseEntity.ok(BaseResponse.success(null, "Account frozen successfully"));
            
        } catch (Exception e) {
            log.error("Failed to freeze account via API: {}", accountId, e);
            throw e;
        }
    }

    /**
     * Unfreeze account
     */
    @PatchMapping("/{accountId}/unfreeze")
    @Operation(summary = "Unfreeze account", description = "Unfreeze account")
    @PreAuthorize("hasRole('ROLE_ACCOUNTS_FREEZE')") // Security check
    @AuditLog
    public ResponseEntity<BaseResponse<Void>> unfreezeAccount(@PathVariable String accountId) {
        log.info("Unfreezing account via API: {}", accountId);

        try {
            // Convert String ID to Long for service call
            Long accountIdLong = Long.parseLong(accountId);
            accountService.unfreezeAccount(accountIdLong);
            
            log.info("Account unfrozen successfully via API: {}", accountId);
            return ResponseEntity.ok(BaseResponse.success(null, "Account unfrozen successfully"));
            
        } catch (Exception e) {
            log.error("Failed to unfreeze account via API: {}", accountId, e);
            throw e;
        }
    }

    // Private helper methods (small and focused)

    /**
     * Build account search request from query parameters
     */
    private AccountSearchRequest buildAccountSearchRequest(String searchTerm, String customerId, 
                                                         String productCode, String branchId, 
                                                         String status, String currency, Boolean active) {
        return AccountSearchRequest.builder()
                .searchTerm(searchTerm)
                .customerId(customerId)
                .productCode(productCode)
                .branchId(branchId) // Will be filtered by service based on user context
                .status(status)
                .currency(currency)
                .active(active)
                .build();
    }
}
