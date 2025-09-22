package com.mutindo.repositories;

import com.mutindo.common.enums.GLAccountType;
import com.mutindo.entities.GLAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * GL Account repository - optimized for chart of accounts operations
 */
@Repository
public interface GLAccountRepository extends JpaRepository<GLAccount, Long> {

    Optional<GLAccount> findByCode(String code);
    
    boolean existsByCode(String code);

    List<GLAccount> findByTypeAndActiveTrue(GLAccountType type);

    // Hierarchical queries
    List<GLAccount> findByParentIdAndActiveTrue(Long parentId);
    
    @Query("SELECT g FROM GLAccount g WHERE g.parentId IS NULL AND g.active = true ORDER BY g.code")
    List<GLAccount> findRootAccounts();

    // Control accounts
    @Query("SELECT g FROM GLAccount g WHERE g.isControlAccount = true AND g.active = true ORDER BY g.code")
    List<GLAccount> findControlAccounts();

    // Posting accounts (leaf accounts)
    @Query("SELECT g FROM GLAccount g WHERE g.allowsPosting = true AND g.active = true ORDER BY g.code")
    List<GLAccount> findPostingAccounts();

    // By level in hierarchy
    List<GLAccount> findByLevelAndActiveTrue(Integer level);
    
    // All active accounts
    List<GLAccount> findByActiveTrue();

    // Search functionality
    @Query("SELECT g FROM GLAccount g WHERE g.active = true AND " +
           "(LOWER(g.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<GLAccount> searchActiveAccounts(@Param("search") String search, Pageable pageable);

    // By type with pagination
    Page<GLAccount> findByTypeAndActiveTrueOrderByCode(GLAccountType type, Pageable pageable);

    // Category-based queries
    List<GLAccount> findByCategoryAndActiveTrue(String category);

    // Currency-specific accounts
    List<GLAccount> findByCurrencyAndActiveTrue(String currency);

    // Validation queries
    @Query("SELECT COUNT(g) FROM GLAccount g WHERE g.parentId = :accountId")
    long countChildAccounts(@Param("accountId") Long accountId);

    @Query("SELECT g FROM GLAccount g WHERE g.code LIKE CONCAT(:codePrefix, '%') AND g.active = true ORDER BY g.code")
    List<GLAccount> findByCodePrefix(@Param("codePrefix") String codePrefix);

    // Account hierarchy path
    @Query(value = "WITH RECURSIVE account_path AS (" +
           "SELECT id, code, name, parent_id, 0 as depth, CAST(code AS VARCHAR(1000)) as path " +
           "FROM gl_accounts WHERE id = :accountId " +
           "UNION ALL " +
           "SELECT p.id, p.code, p.name, p.parent_id, ap.depth + 1, CONCAT(p.code, ' > ', ap.path) " +
           "FROM gl_accounts p JOIN account_path ap ON p.id = ap.parent_id) " +
           "SELECT * FROM account_path ORDER BY depth DESC", nativeQuery = true)
    List<Object[]> getAccountHierarchyPath(@Param("accountId") Long accountId);

    // Count methods for statistics
    @Query("SELECT COUNT(g) FROM GLAccount g WHERE g.type = :type AND g.active = true")
    long countByTypeAndActiveTrue(@Param("type") GLAccountType type);

    @Query("SELECT COUNT(g) FROM GLAccount g WHERE g.active = true")
    long countByActiveTrue();
}
