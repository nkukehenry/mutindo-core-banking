package com.mutindo.chartofaccounts.service;

import com.mutindo.chartofaccounts.dto.GLAccountDto;
import com.mutindo.chartofaccounts.mapper.GLAccountMapper;
import com.mutindo.common.enums.GLAccountType;
import com.mutindo.entities.GLAccount;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.GLAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * GL Account query service - focused on search and query operations
 * Handles all read-only operations with caching for performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GLAccountQueryService {

    private final GLAccountRepository glAccountRepository;
    private final GLAccountMapper glAccountMapper;

    /**
     * Get GL account by code - cached
     */
    @Cacheable(value = "glAccounts", key = "#code")
    @PerformanceLog
    public Optional<GLAccountDto> getAccountByCode(String code) {
        log.debug("Getting account by code: {}", code);
        
        return glAccountRepository.findByCode(code)
                .map(glAccountMapper::toDto);
    }

    /**
     * Get GL account by ID - cached
     */
    @Cacheable(value = "glAccounts", key = "#id")
    @PerformanceLog
    public Optional<GLAccountDto> getAccountById(Long id) {
        log.debug("Getting account by ID: {}", id);
        
        return glAccountRepository.findById(id)
                .map(glAccountMapper::toDto);
    }

    /**
     * Get accounts by type - cached
     */
    @Cacheable(value = "glAccountsByType", key = "#type")
    @PerformanceLog
    public List<GLAccountDto> getAccountsByType(GLAccountType type) {
        log.debug("Getting accounts by type: {}", type);
        
        return glAccountRepository.findByTypeAndActiveTrue(type)
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get control accounts - cached
     */
    @Cacheable(value = "controlAccounts")
    @PerformanceLog
    public List<GLAccountDto> getControlAccounts() {
        log.debug("Getting control accounts");
        
        return glAccountRepository.findControlAccounts()
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get posting accounts (leaf accounts) - cached
     */
    @Cacheable(value = "postingAccounts")
    @PerformanceLog
    public List<GLAccountDto> getPostingAccounts() {
        log.debug("Getting posting accounts");
        
        return glAccountRepository.findPostingAccounts()
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by parent ID
     */
    @Cacheable(value = "glAccountsByParent", key = "#parentId")
    @PerformanceLog
    public List<GLAccountDto> getAccountsByParent(Long parentId) {
        log.debug("Getting accounts by parent ID: {}", parentId);
        
        return glAccountRepository.findByParentIdAndActiveTrue(parentId)
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by currency
     */
    @Cacheable(value = "glAccountsByCurrency", key = "#currency")
    @PerformanceLog
    public List<GLAccountDto> getAccountsByCurrency(String currency) {
        log.debug("Getting accounts by currency: {}", currency);
        
        return glAccountRepository.findByCurrencyAndActiveTrue(currency)
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by category
     */
    @Cacheable(value = "glAccountsByCategory", key = "#category")
    @PerformanceLog
    public List<GLAccountDto> getAccountsByCategory(String category) {
        log.debug("Getting accounts by category: {}", category);
        
        return glAccountRepository.findByCategoryAndActiveTrue(category)
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search accounts with pagination
     */
    @PerformanceLog
    public Page<GLAccountDto> searchAccounts(String searchTerm, Pageable pageable) {
        log.debug("Searching accounts with term: {}", searchTerm);
        
        return glAccountRepository.searchActiveAccounts(searchTerm, pageable)
                .map(glAccountMapper::toDto);
    }

    /**
     * Get all active accounts
     */
    @Cacheable(value = "allActiveAccounts")
    @PerformanceLog
    public List<GLAccountDto> getAllActiveAccounts() {
        log.debug("Getting all active accounts");
        
        return glAccountRepository.findByActiveTrue()
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by level
     */
    @Cacheable(value = "glAccountsByLevel", key = "#level")
    @PerformanceLog
    public List<GLAccountDto> getAccountsByLevel(Integer level) {
        log.debug("Getting accounts by level: {}", level);
        
        return glAccountRepository.findByLevelAndActiveTrue(level)
                .stream()
                .map(glAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get root accounts (level 0)
     */
    @Cacheable(value = "rootAccounts")
    @PerformanceLog
    public List<GLAccountDto> getRootAccounts() {
        log.debug("Getting root accounts");
        
        return getAccountsByLevel(0);
    }

    /**
     * Check if account exists by code
     */
    @PerformanceLog
    public boolean accountExistsByCode(String code) {
        return glAccountRepository.existsByCode(code);
    }

    /**
     * Check if account exists by ID
     */
    @PerformanceLog
    public boolean accountExistsById(Long id) {
        return glAccountRepository.existsById(id);
    }

    /**
     * Count accounts by type
     */
    @PerformanceLog
    public long countAccountsByType(GLAccountType type) {
        return glAccountRepository.countByTypeAndActiveTrue(type);
    }

    /**
     * Count total active accounts
     */
    @PerformanceLog
    public long countActiveAccounts() {
        return glAccountRepository.countByActiveTrue();
    }
}
