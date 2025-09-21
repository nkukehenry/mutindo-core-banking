package com.mutindo.chartofaccounts.service;

import com.mutindo.chartofaccounts.dto.CreateGLAccountRequest;
import com.mutindo.chartofaccounts.dto.GLAccountDto;
import com.mutindo.chartofaccounts.dto.GLAccountHierarchyDto;
import com.mutindo.common.enums.GLAccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Chart of Accounts service interface for polymorphism and testability
 */
public interface IChartOfAccountsService {
    
    /**
     * Get complete chart of accounts hierarchy
     */
    List<GLAccountHierarchyDto> getChartOfAccountsHierarchy();
    
    /**
     * Get GL account by code
     */
    Optional<GLAccountDto> getAccountByCode(String code);
    
    /**
     * Get accounts by type
     */
    List<GLAccountDto> getAccountsByType(GLAccountType type);
    
    /**
     * Get control accounts
     */
    List<GLAccountDto> getControlAccounts();
    
    /**
     * Get posting accounts (leaf accounts)
     */
    List<GLAccountDto> getPostingAccounts();
    
    /**
     * Create new GL account
     */
    GLAccountDto createAccount(CreateGLAccountRequest request);
    
    /**
     * Update GL account
     */
    GLAccountDto updateAccount(Long accountId, CreateGLAccountRequest request);
    
    /**
     * Deactivate account (soft delete)
     */
    void deactivateAccount(Long accountId);
    
    /**
     * Search accounts with pagination
     */
    Page<GLAccountDto> searchAccounts(String searchTerm, Pageable pageable);
    
    /**
     * Get account hierarchy path
     */
    List<GLAccountDto> getAccountHierarchyPath(Long accountId);
    
    /**
     * Async hierarchy update
     */
    CompletableFuture<Void> updateHierarchyAsync(Long accountId);
}
