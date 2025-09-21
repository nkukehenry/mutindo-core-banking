package com.mutindo.chartofaccounts.service;

import com.mutindo.chartofaccounts.dto.GLAccountDto;
import com.mutindo.chartofaccounts.dto.GLAccountHierarchyDto;
import com.mutindo.chartofaccounts.dto.CreateGLAccountRequest;
import com.mutindo.chartofaccounts.mapper.GLAccountMapper;
import com.mutindo.common.enums.GLAccountType;
import com.mutindo.entities.GLAccount;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.GLAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Chart of Accounts service implementation with caching and async processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChartOfAccountsService implements IChartOfAccountsService {

    private final GLAccountRepository glAccountRepository;
    private final GLAccountMapper glAccountMapper;

    /**
     * Get complete chart of accounts hierarchy - cached for performance
     */
    @Cacheable(value = "chartOfAccounts", key = "'hierarchy'")
    @PerformanceLog
    public List<GLAccountHierarchyDto> getChartOfAccountsHierarchy() {
        log.info("Building chart of accounts hierarchy");
        
        List<GLAccount> allAccounts = glAccountRepository.findByActiveTrue();
        return buildHierarchy(allAccounts, null);
    }

    /**
     * Get GL account by code - cached
     */
    @Cacheable(value = "glAccounts", key = "#code")
    public Optional<GLAccountDto> getAccountByCode(String code) {
        return glAccountRepository.findByCode(code)
                .map(glAccountMapper::toDto);
    }

    /**
     * Get accounts by type - cached
     */
    @Cacheable(value = "glAccountsByType", key = "#type")
    public List<GLAccountDto> getAccountsByType(GLAccountType type) {
        return glAccountRepository.findByTypeAndActiveTrue(type)
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get control accounts - cached
     */
    @Cacheable(value = "controlAccounts")
    public List<GLAccountDto> getControlAccounts() {
        return glAccountRepository.findControlAccounts()
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get posting accounts (leaf accounts) - cached
     */
    @Cacheable(value = "postingAccounts")
    public List<GLAccountDto> getPostingAccounts() {
        return glAccountRepository.findPostingAccounts()
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create new GL account with validation and caching eviction
     */
    @Transactional
    @AuditLog
    @CacheEvict(value = {"chartOfAccounts", "glAccountsByType", "controlAccounts", "postingAccounts"}, allEntries = true)
    public GLAccountDto createAccount(CreateGLAccountRequest request) {
        log.info("Creating GL account with code: {}", request.getCode());
        
        // Validation
        validateCreateAccountRequest(request);
        
        // Check for duplicate code
        if (glAccountRepository.existsByCode(request.getCode())) {
            throw new ValidationException("GL Account code already exists: " + request.getCode());
        }
        
        // Validate parent account if specified
        GLAccount parentAccount = null;
        if (request.getParentId() != null) {
            parentAccount = glAccountRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent account not found", "PARENT_ACCOUNT_NOT_FOUND"));
            
            // Validate parent-child relationship
            validateParentChildRelationship(parentAccount, request);
        }
        
        // Create account
        GLAccount account = glAccountMapper.toEntity(request);
        account.setLevel(calculateAccountLevel(parentAccount));
        
        // Set control account flags based on business rules
        setControlAccountFlags(account, parentAccount);
        
        GLAccount savedAccount = glAccountRepository.save(account);
        
        // Async operations
        updateHierarchyAsync(savedAccount.getId());
        
        return glAccountMapper.toDto(savedAccount);
    }

    /**
     * Update GL account
     */
    @Transactional
    @AuditLog
    @CacheEvict(value = {"chartOfAccounts", "glAccounts", "glAccountsByType", "controlAccounts", "postingAccounts"}, allEntries = true)
    public GLAccountDto updateAccount(Long accountId, CreateGLAccountRequest request) {
        log.info("Updating GL account: {}", accountId);
        
        GLAccount existingAccount = glAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("GL Account not found", "ACCOUNT_NOT_FOUND"));
        
        // Validate update
        validateUpdateAccountRequest(existingAccount, request);
        
        // Update fields
        glAccountMapper.updateEntity(existingAccount, request);
        
        GLAccount savedAccount = glAccountRepository.save(existingAccount);
        
        return glAccountMapper.toDto(savedAccount);
    }

    /**
     * Deactivate account (soft delete)
     */
    @Transactional
    @AuditLog
    @CacheEvict(value = {"chartOfAccounts", "glAccounts", "glAccountsByType", "controlAccounts", "postingAccounts"}, allEntries = true)
    public void deactivateAccount(Long accountId) {
        log.info("Deactivating GL account: {}", accountId);
        
        GLAccount account = glAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("GL Account not found", "ACCOUNT_NOT_FOUND"));
        
        // Check if account has child accounts
        long childCount = glAccountRepository.countChildAccounts(accountId);
        if (childCount > 0) {
            throw new BusinessException("Cannot deactivate account with child accounts", "HAS_CHILD_ACCOUNTS");
        }
        
        // Check if account has been used in transactions (implement based on business rules)
        // validateAccountNotUsedInTransactions(accountId);
        
        account.setActive(false);
        glAccountRepository.save(account);
    }

    /**
     * Search accounts with pagination
     */
    public Page<GLAccountDto> searchAccounts(String searchTerm, Pageable pageable) {
        return glAccountRepository.searchActiveAccounts(searchTerm, pageable)
                .map(glAccountMapper::toDto);
    }

    /**
     * Get account hierarchy path for a specific account
     */
    @Cacheable(value = "accountHierarchyPath", key = "#accountId")
    public List<GLAccountDto> getAccountHierarchyPath(Long accountId) {
        List<Object[]> pathData = glAccountRepository.getAccountHierarchyPath(accountId);
        
        return pathData.stream()
                .map(row -> GLAccountDto.builder()
                        .id((Long) row[0])
                        .code((String) row[1])
                        .name((String) row[2])
                        .level((Integer) row[3])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Async method to update hierarchy after account changes
     */
    @Override
    @Async
    public CompletableFuture<Void> updateHierarchyAsync(Long accountId) {
        GLAccount account = glAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", "ACCOUNT_NOT_FOUND"));
        log.info("Updating hierarchy asynchronously for account: {}", account.getCode());
        
        try {
            // Update child account levels if this is a parent
            updateChildAccountLevels(account);
            
            // Refresh related caches
            refreshRelatedCaches();
            
            log.info("Hierarchy update completed for account: {}", account.getCode());
        } catch (Exception e) {
            log.error("Failed to update hierarchy for account: {}", account.getCode(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // Private helper methods

    private List<GLAccountHierarchyDto> buildHierarchy(List<GLAccount> accounts, Long parentId) {
        return accounts.stream()
                .filter(account -> Objects.equals(account.getParentId(), parentId))
                .map(account -> {
                    GLAccountHierarchyDto dto = GLAccountHierarchyDto.builder()
                            .id(account.getId())
                            .code(account.getCode())
                            .name(account.getName())
                            .type(account.getType())
                            .level(account.getLevel())
                            .isControlAccount(account.getIsControlAccount())
                            .allowsPosting(account.getAllowsPosting())
                            .currency(account.getCurrency())
                            .children(buildHierarchy(accounts, account.getId()))
                            .build();
                    
                    return dto;
                })
                .sorted(Comparator.comparing(GLAccountHierarchyDto::getCode))
                .collect(Collectors.toList());
    }

    private void validateCreateAccountRequest(CreateGLAccountRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new ValidationException("Account code is required");
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("Account name is required");
        }
        
        if (request.getType() == null) {
            throw new ValidationException("Account type is required");
        }
        
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            throw new ValidationException("Currency is required");
        }
    }

    private void validateParentChildRelationship(GLAccount parentAccount, CreateGLAccountRequest request) {
        // Validate account type hierarchy rules
        if (parentAccount.getType() != request.getType()) {
            // Allow some flexibility in hierarchy but enforce basic rules
            validateAccountTypeHierarchy(parentAccount.getType(), request.getType());
        }
        
        // Validate currency consistency
        if (!parentAccount.getCurrency().equals(request.getCurrency())) {
            throw new ValidationException("Child account currency must match parent account currency");
        }
    }

    private void validateAccountTypeHierarchy(GLAccountType parentType, GLAccountType childType) {
        // Implement business rules for account type hierarchy
        // For example: ASSET parent can have ASSET children, etc.
        if (parentType != childType) {
            throw new ValidationException("Account type mismatch with parent account");
        }
    }

    private void validateUpdateAccountRequest(GLAccount existingAccount, CreateGLAccountRequest request) {
        // Cannot change account code
        if (!existingAccount.getCode().equals(request.getCode())) {
            throw new ValidationException("Cannot change account code");
        }
        
        // Cannot change account type if account has been used
        if (existingAccount.getType() != request.getType()) {
            // Check if account has been used in transactions
            // validateAccountNotUsedInTransactions(existingAccount.getId());
        }
    }

    private Integer calculateAccountLevel(GLAccount parentAccount) {
        if (parentAccount == null) {
            return 0; // Root level
        }
        return parentAccount.getLevel() + 1;
    }

    private void setControlAccountFlags(GLAccount account, GLAccount parentAccount) {
        // Business logic for setting control account flags
        // Control accounts typically don't allow direct posting
        if (account.getIsControlAccount()) {
            account.setAllowsPosting(false);
        }
    }

    private void updateChildAccountLevels(GLAccount parentAccount) {
        List<GLAccount> childAccounts = glAccountRepository.findByParentIdAndActiveTrue(parentAccount.getId());
        
        for (GLAccount child : childAccounts) {
            child.setLevel(parentAccount.getLevel() + 1);
            glAccountRepository.save(child);
            
            // Recursively update grandchildren
            updateChildAccountLevels(child);
        }
    }

    private void refreshRelatedCaches() {
        // Implementation would trigger cache refresh for related services
        log.info("Refreshing related caches after hierarchy update");
    }
}
