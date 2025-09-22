package com.mutindo.chartofaccounts.service;

import com.mutindo.chartofaccounts.dto.GLAccountDto;
import com.mutindo.chartofaccounts.dto.GLAccountHierarchyDto;
import com.mutindo.chartofaccounts.dto.CreateGLAccountRequest;
import com.mutindo.chartofaccounts.mapper.GLAccountMapper;
import com.mutindo.common.enums.GLAccountType;
import com.mutindo.entities.GLAccount;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.GLAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Chart of Accounts service implementation - refactored for maintainability
 * Delegates to specialized services for better separation of concerns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChartOfAccountsService implements IChartOfAccountsService {

    private final GLAccountRepository glAccountRepository;
    private final GLAccountMapper glAccountMapper;
    
    // Specialized services for better separation of concerns
    private final GLAccountValidationService validationService;
    private final GLAccountHierarchyService hierarchyService;
    private final GLAccountQueryService queryService;

    /**
     * Get complete chart of accounts hierarchy - delegated to hierarchy service
     */
    @Override
    @PerformanceLog
    public List<GLAccountHierarchyDto> getChartOfAccountsHierarchy() {
        return hierarchyService.buildHierarchy();
    }

    /**
     * Get GL account by code - delegated to query service
     */
    @Override
    public Optional<GLAccountDto> getAccountByCode(String code) {
        return queryService.getAccountByCode(code);
    }

    /**
     * Get accounts by type - delegated to query service
     */
    @Override
    public List<GLAccountDto> getAccountsByType(GLAccountType type) {
        return queryService.getAccountsByType(type);
    }

    /**
     * Get control accounts - delegated to query service
     */
    @Override
    public List<GLAccountDto> getControlAccounts() {
        return queryService.getControlAccounts();
    }

    /**
     * Get posting accounts (leaf accounts) - delegated to query service
     */
    @Override
    public List<GLAccountDto> getPostingAccounts() {
        return queryService.getPostingAccounts();
    }

    /**
     * Create new GL account with validation and caching eviction
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"chartOfAccounts", "glAccounts", "glAccountsByType", "controlAccounts", "postingAccounts", "glAccountsByParent", "glAccountsByCurrency", "glAccountsByCategory", "glAccountsByLevel", "rootAccounts", "allActiveAccounts"}, allEntries = true)
    public GLAccountDto createAccount(CreateGLAccountRequest request) {
        log.info("Creating GL account with code: {}", request.getCode());
        
        // Validate request using validation service
        validationService.validateCreateAccountRequest(request);
        
        // Validate parent account if specified
        GLAccount parentAccount = null;
        if (request.getParentId() != null) {
            parentAccount = glAccountRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent account not found", "PARENT_ACCOUNT_NOT_FOUND"));
            
            // Validate parent-child relationship using validation service
            validationService.validateParentChildRelationship(parentAccount, request);
        }
        
        // Create account
        GLAccount account = glAccountMapper.toEntity(request);
        account.setLevel(hierarchyService.calculateAccountLevel(parentAccount));
        
        // Set control account flags based on business rules
        setControlAccountFlags(account, parentAccount);
        
        GLAccount savedAccount = glAccountRepository.save(account);
        
        // Async hierarchy update
        hierarchyService.updateHierarchyAsync(savedAccount.getId());
        
        log.info("GL account created successfully: {}", savedAccount.getCode());
        return glAccountMapper.toDto(savedAccount);
    }

    /**
     * Update GL account
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"chartOfAccounts", "glAccounts", "glAccountsByType", "controlAccounts", "postingAccounts", "glAccountsByParent", "glAccountsByCurrency", "glAccountsByCategory", "glAccountsByLevel", "rootAccounts", "allActiveAccounts"}, allEntries = true)
    public GLAccountDto updateAccount(Long accountId, CreateGLAccountRequest request) {
        log.info("Updating GL account: {}", accountId);
        
        GLAccount existingAccount = glAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("GL Account not found", "ACCOUNT_NOT_FOUND"));
        
        // Validate update using validation service
        validationService.validateUpdateAccountRequest(existingAccount, request);
        
        // Update fields
        glAccountMapper.updateEntity(existingAccount, request);
        
        GLAccount savedAccount = glAccountRepository.save(existingAccount);
        
        log.info("GL account updated successfully: {}", savedAccount.getCode());
        return glAccountMapper.toDto(savedAccount);
    }

    /**
     * Deactivate account (soft delete)
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"chartOfAccounts", "glAccounts", "glAccountsByType", "controlAccounts", "postingAccounts", "glAccountsByParent", "glAccountsByCurrency", "glAccountsByCategory", "glAccountsByLevel", "rootAccounts", "allActiveAccounts"}, allEntries = true)
    public void deactivateAccount(Long accountId) {
        log.info("Deactivating GL account: {}", accountId);
        
        // Validate deactivation using validation service
        validationService.validateAccountDeactivation(accountId);
        
        GLAccount account = glAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("GL Account not found", "ACCOUNT_NOT_FOUND"));
        
        account.setActive(false);
        glAccountRepository.save(account);
        
        log.info("GL account deactivated successfully: {}", account.getCode());
    }

    /**
     * Search accounts with pagination - delegated to query service
     */
    @Override
    public Page<GLAccountDto> searchAccounts(String searchTerm, Pageable pageable) {
        return queryService.searchAccounts(searchTerm, pageable);
    }

    /**
     * Get account hierarchy path - delegated to hierarchy service
     */
    @Override
    public List<GLAccountDto> getAccountHierarchyPath(Long accountId) {
        return hierarchyService.getAccountHierarchyPath(accountId)
                .stream()
                .map(hierarchyDto -> GLAccountDto.builder()
                        .id(hierarchyDto.getId())
                        .code(hierarchyDto.getCode())
                        .name(hierarchyDto.getName())
                        .level(hierarchyDto.getLevel())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Async method to update hierarchy after account changes - delegated to hierarchy service
     */
    @Override
    public CompletableFuture<Void> updateHierarchyAsync(Long accountId) {
        return hierarchyService.updateHierarchyAsync(accountId);
    }

    // Private helper methods

    /**
     * Set control account flags based on business rules
     */
    private void setControlAccountFlags(GLAccount account, GLAccount parentAccount) {
        // Business logic for setting control account flags
        // Control accounts typically don't allow direct posting
        if (account.getIsControlAccount()) {
            account.setAllowsPosting(false);
        }
    }
}
