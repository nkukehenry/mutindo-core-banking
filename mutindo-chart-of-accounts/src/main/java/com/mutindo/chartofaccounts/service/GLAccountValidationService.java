package com.mutindo.chartofaccounts.service;

import com.mutindo.chartofaccounts.dto.CreateGLAccountRequest;
import com.mutindo.common.enums.GLAccountType;
import com.mutindo.entities.GLAccount;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.GLAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * GL Account validation service - focused on business rules validation
 * Centralizes all validation logic for maintainability and testability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GLAccountValidationService {

    private final GLAccountRepository glAccountRepository;

    /**
     * Validate account creation request
     */
    @PerformanceLog
    public void validateCreateAccountRequest(CreateGLAccountRequest request) {
        log.debug("Validating account creation request for code: {}", request.getCode());
        
        validateBasicFields(request);
        validateAccountCode(request.getCode());
        validateAccountName(request.getName());
        validateAccountType(request.getType());
        validateCurrency(request.getCurrency());
        
        log.debug("Account creation request validation completed");
    }

    /**
     * Validate account update request
     */
    @PerformanceLog
    public void validateUpdateAccountRequest(GLAccount existingAccount, CreateGLAccountRequest request) {
        log.debug("Validating account update request for account: {}", existingAccount.getId());
        
        // Cannot change account code
        if (!existingAccount.getCode().equals(request.getCode())) {
            throw new ValidationException("Cannot change account code");
        }
        
        // Cannot change account type if account has been used in transactions
        if (existingAccount.getType() != request.getType()) {
            validateAccountTypeChange(existingAccount.getId());
        }
        
        // Validate other fields
        validateAccountName(request.getName());
        validateCurrency(request.getCurrency());
        
        log.debug("Account update request validation completed");
    }

    /**
     * Validate parent-child relationship
     */
    @PerformanceLog
    public void validateParentChildRelationship(GLAccount parentAccount, CreateGLAccountRequest request) {
        log.debug("Validating parent-child relationship for parent: {} and child: {}", 
                 parentAccount.getCode(), request.getCode());
        
        // Validate account type hierarchy rules
        validateAccountTypeHierarchy(parentAccount.getType(), request.getType());
        
        // Validate currency consistency
        validateCurrencyConsistency(parentAccount.getCurrency(), request.getCurrency());
        
        // Validate hierarchy depth
        validateHierarchyDepth(parentAccount);
        
        log.debug("Parent-child relationship validation completed");
    }

    /**
     * Validate account can be deactivated
     */
    @PerformanceLog
    public void validateAccountDeactivation(Long accountId) {
        log.debug("Validating account deactivation for account: {}", accountId);
        
        GLAccount account = glAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("GL Account not found", "ACCOUNT_NOT_FOUND"));
        
        // Check if account has child accounts
        validateNoChildAccounts(accountId);
        
        // Check if account has been used in transactions
        validateAccountNotUsedInTransactions(accountId);
        
        // Check if account is a system account
        validateNotSystemAccount(account);
        
        log.debug("Account deactivation validation completed");
    }

    /**
     * Validate account code format and uniqueness
     */
    @PerformanceLog
    public void validateAccountCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Account code is required");
        }
        
        if (code.length() < 3 || code.length() > 20) {
            throw new ValidationException("Account code must be between 3 and 20 characters");
        }
        
        if (!code.matches("^[A-Z0-9]+$")) {
            throw new ValidationException("Account code must contain only uppercase letters and numbers");
        }
        
        if (glAccountRepository.existsByCode(code)) {
            throw new ValidationException("GL Account code already exists: " + code);
        }
    }

    /**
     * Validate account name
     */
    @PerformanceLog
    public void validateAccountName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Account name is required");
        }
        
        if (name.length() < 3 || name.length() > 100) {
            throw new ValidationException("Account name must be between 3 and 100 characters");
        }
    }

    /**
     * Validate account type
     */
    @PerformanceLog
    public void validateAccountType(GLAccountType type) {
        if (type == null) {
            throw new ValidationException("Account type is required");
        }
    }

    /**
     * Validate currency
     */
    @PerformanceLog
    public void validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new ValidationException("Currency is required");
        }
        
        if (currency.length() != 3) {
            throw new ValidationException("Currency must be a 3-letter ISO code");
        }
        
        if (!currency.matches("^[A-Z]{3}$")) {
            throw new ValidationException("Currency must be uppercase letters only");
        }
    }

    // Private validation methods

    private void validateBasicFields(CreateGLAccountRequest request) {
        if (request == null) {
            throw new ValidationException("Account request cannot be null");
        }
    }

    private void validateAccountTypeHierarchy(GLAccountType parentType, GLAccountType childType) {
        // Implement business rules for account type hierarchy
        // For example: ASSET parent can have ASSET children, etc.
        if (parentType != childType) {
            throw new ValidationException("Account type mismatch with parent account");
        }
    }

    private void validateCurrencyConsistency(String parentCurrency, String childCurrency) {
        if (!parentCurrency.equals(childCurrency)) {
            throw new ValidationException("Child account currency must match parent account currency");
        }
    }

    private void validateHierarchyDepth(GLAccount parentAccount) {
        // Maximum hierarchy depth of 5 levels
        if (parentAccount.getLevel() >= 4) {
            throw new ValidationException("Maximum hierarchy depth exceeded");
        }
    }

    private void validateNoChildAccounts(Long accountId) {
        long childCount = glAccountRepository.countChildAccounts(accountId);
        if (childCount > 0) {
            throw new BusinessException("Cannot deactivate account with child accounts", "HAS_CHILD_ACCOUNTS");
        }
    }

    private void validateAccountNotUsedInTransactions(Long accountId) {
        // TODO: Implement when transaction repositories are available
        // This would check if the account has been used in any journal entries
        log.debug("Transaction usage validation not yet implemented for account: {}", accountId);
    }

    private void validateAccountTypeChange(Long accountId) {
        // TODO: Implement when transaction repositories are available
        // This would check if the account has been used in any transactions
        log.debug("Account type change validation not yet implemented for account: {}", accountId);
    }

    private void validateNotSystemAccount(GLAccount account) {
        // System accounts cannot be deactivated
        if (account.getIsControlAccount() && account.getCode().startsWith("SYS_")) {
            throw new BusinessException("Cannot deactivate system account", "SYSTEM_ACCOUNT");
        }
    }
}
