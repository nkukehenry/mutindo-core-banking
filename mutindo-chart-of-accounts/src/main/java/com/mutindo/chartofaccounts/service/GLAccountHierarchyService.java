package com.mutindo.chartofaccounts.service;

import com.mutindo.chartofaccounts.dto.GLAccountHierarchyDto;
import com.mutindo.entities.GLAccount;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.GLAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * GL Account hierarchy service - focused on hierarchy management
 * Handles all hierarchy-related operations and caching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GLAccountHierarchyService {

    private final GLAccountRepository glAccountRepository;

    /**
     * Build complete chart of accounts hierarchy - cached for performance
     */
    @Cacheable(value = "chartOfAccounts", key = "'hierarchy'")
    @PerformanceLog
    public List<GLAccountHierarchyDto> buildHierarchy() {
        log.info("Building chart of accounts hierarchy");
        
        List<GLAccount> allAccounts = glAccountRepository.findByActiveTrue();
        List<GLAccountHierarchyDto> hierarchy = buildHierarchyRecursive(allAccounts, null);
        
        log.info("Built hierarchy with {} root accounts", hierarchy.size());
        return hierarchy;
    }

    /**
     * Get account hierarchy path for a specific account
     */
    @Cacheable(value = "accountHierarchyPath", key = "#accountId")
    @PerformanceLog
    public List<GLAccountHierarchyDto> getAccountHierarchyPath(Long accountId) {
        log.debug("Getting hierarchy path for account: {}", accountId);
        
        List<Object[]> pathData = glAccountRepository.getAccountHierarchyPath(accountId);
        
        return pathData.stream()
                .map(row -> GLAccountHierarchyDto.builder()
                        .id((Long) row[0])
                        .code((String) row[1])
                        .name((String) row[2])
                        .level((Integer) row[3])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Calculate account level based on parent
     */
    @PerformanceLog
    public Integer calculateAccountLevel(GLAccount parentAccount) {
        if (parentAccount == null) {
            return 0; // Root level
        }
        return parentAccount.getLevel() + 1;
    }

    /**
     * Update hierarchy after account changes
     */
    @Async
    @CacheEvict(value = {"chartOfAccounts", "accountHierarchyPath"}, allEntries = true)
    public CompletableFuture<Void> updateHierarchyAsync(Long accountId) {
        log.info("Updating hierarchy asynchronously for account: {}", accountId);
        
        try {
            GLAccount account = glAccountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
            
            // Update child account levels if this is a parent
            updateChildAccountLevels(account);
            
            // Refresh related caches
            refreshRelatedCaches();
            
            log.info("Hierarchy update completed for account: {}", account.getCode());
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("Failed to update hierarchy for account: {}", accountId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get all child accounts for a parent account
     */
    @PerformanceLog
    public List<GLAccount> getChildAccounts(Long parentId) {
        return glAccountRepository.findByParentIdAndActiveTrue(parentId);
    }

    /**
     * Get all descendant accounts for a parent account
     */
    @PerformanceLog
    public List<GLAccount> getDescendantAccounts(Long parentId) {
        List<GLAccount> descendants = getChildAccounts(parentId);
        
        for (GLAccount child : getChildAccounts(parentId)) {
            descendants.addAll(getDescendantAccounts(child.getId()));
        }
        
        return descendants;
    }

    /**
     * Check if account is a leaf account (has no children)
     */
    @PerformanceLog
    public boolean isLeafAccount(Long accountId) {
        return glAccountRepository.countChildAccounts(accountId) == 0;
    }

    /**
     * Get account depth in hierarchy
     */
    @PerformanceLog
    public int getAccountDepth(Long accountId) {
        List<GLAccountHierarchyDto> path = getAccountHierarchyPath(accountId);
        return path.size() - 1; // Subtract 1 because path includes the account itself
    }

    // Private helper methods

    /**
     * Recursively build hierarchy from flat account list
     */
    private List<GLAccountHierarchyDto> buildHierarchyRecursive(List<GLAccount> accounts, Long parentId) {
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
                            .children(buildHierarchyRecursive(accounts, account.getId()))
                            .build();
                    
                    return dto;
                })
                .sorted(Comparator.comparing(GLAccountHierarchyDto::getCode))
                .collect(Collectors.toList());
    }

    /**
     * Update child account levels recursively
     */
    @Transactional
    private void updateChildAccountLevels(GLAccount parentAccount) {
        List<GLAccount> childAccounts = getChildAccounts(parentAccount.getId());
        
        for (GLAccount child : childAccounts) {
            child.setLevel(parentAccount.getLevel() + 1);
            glAccountRepository.save(child);
            
            // Recursively update grandchildren
            updateChildAccountLevels(child);
        }
    }

    /**
     * Refresh related caches after hierarchy updates
     */
    private void refreshRelatedCaches() {
        // Implementation would trigger cache refresh for related services
        log.info("Refreshing related caches after hierarchy update");
    }
}
