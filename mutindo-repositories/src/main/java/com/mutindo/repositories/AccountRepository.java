package com.mutindo.repositories;

import com.mutindo.common.enums.AccountStatus;
import com.mutindo.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Account repository - focused only on data access
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    Page<Account> findByCustomerId(Long customerId, Pageable pageable);

    List<Account> findByBranchId(Long branchId);

    Page<Account> findByBranchId(Long branchId, Pageable pageable);

    List<Account> findByStatus(AccountStatus status);

    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.branchId = :branchId AND a.status = :status")
    Page<Account> findByBranchAndStatus(@Param("branchId") Long branchId, 
                                       @Param("status") AccountStatus status, 
                                       Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.status = :status")
    List<Account> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                           @Param("status") AccountStatus status);

    @Query("SELECT a FROM Account a JOIN Product p ON a.productId = p.id WHERE " +
           "(LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Account> searchAccounts(@Param("search") String search, Pageable pageable);

    @Query("SELECT a FROM Account a JOIN Product p ON a.productId = p.id WHERE a.branchId = :branchId AND " +
           "(LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Account> searchAccountsByBranch(@Param("branchId") Long branchId,
                                        @Param("search") String search, 
                                        Pageable pageable);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.branchId = :branchId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByBranch(@Param("branchId") Long branchId);

    // Temporarily commented out due to query compilation issues
    // @Query("SELECT SUM(a.balance) FROM Account a WHERE a.productId = :productId AND a.status = 'ACTIVE'")
    // BigDecimal getTotalBalanceByProduct(@Param("productId") Long productId);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.branchId = :branchId AND a.status = 'ACTIVE'")
    long countActiveAccountsByBranch(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    long countActiveAccountsByCustomer(@Param("customerId") Long customerId);
}
