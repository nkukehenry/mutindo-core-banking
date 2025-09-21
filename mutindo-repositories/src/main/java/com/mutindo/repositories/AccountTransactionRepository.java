package com.mutindo.repositories;

import com.mutindo.common.enums.TransactionStatus;
import com.mutindo.common.enums.TransactionType;
import com.mutindo.entities.AccountTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Account Transaction repository - optimized for high-volume transaction processing
 */
@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    // Idempotency check - critical for transaction processing
    Optional<AccountTransaction> findByIdempotencyKey(String idempotencyKey);
    
    boolean existsByIdempotencyKey(String idempotencyKey);

    // Account transaction history - optimized with pagination
    Page<AccountTransaction> findByAccountIdOrderByCreatedAtDesc(String accountId, Pageable pageable);

    @Query("SELECT t FROM AccountTransaction t WHERE t.accountId = :accountId AND t.status = :status ORDER BY t.createdAt DESC")
    Page<AccountTransaction> findByAccountIdAndStatusOrderByCreatedAtDesc(
            @Param("accountId") String accountId, 
            @Param("status") TransactionStatus status, 
            Pageable pageable);

    // Transaction type filtering
    Page<AccountTransaction> findByAccountIdAndTxTypeOrderByCreatedAtDesc(
            String accountId, TransactionType txType, Pageable pageable);

    // Date range queries - optimized with indexes
    @Query("SELECT t FROM AccountTransaction t WHERE t.accountId = :accountId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<AccountTransaction> findByAccountIdAndDateRange(
            @Param("accountId") String accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Pending transactions - for processing
    @Query("SELECT t FROM AccountTransaction t WHERE t.status = 'PENDING' AND t.createdAt < :cutoffTime")
    List<AccountTransaction> findPendingTransactionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Failed transactions for retry
    @Query("SELECT t FROM AccountTransaction t WHERE t.status = 'FAILED' AND t.createdAt > :retryAfter")
    List<AccountTransaction> findFailedTransactionsForRetry(@Param("retryAfter") LocalDateTime retryAfter);

    // Transaction volume analytics
    @Query("SELECT COUNT(t) FROM AccountTransaction t WHERE t.accountId = :accountId " +
           "AND t.status = 'POSTED' AND t.createdAt BETWEEN :startDate AND :endDate")
    long countPostedTransactionsByAccountAndDateRange(
            @Param("accountId") String accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM AccountTransaction t WHERE t.accountId = :accountId " +
           "AND t.txType = :txType AND t.status = 'POSTED' AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByAccountTypeAndDateRange(
            @Param("accountId") String accountId,
            @Param("txType") TransactionType txType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Branch-level analytics
    @Query("SELECT t FROM AccountTransaction t WHERE t.accountId IN " +
           "(SELECT CAST(a.id AS string) FROM Account a WHERE a.branchId = :branchId) " +
           "AND t.status = 'POSTED' AND t.createdAt BETWEEN :startDate AND :endDate")
    Page<AccountTransaction> findPostedTransactionsByBranchAndDateRange(
            @Param("branchId") Long branchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Channel analytics
    @Query("SELECT t.channel, COUNT(t), SUM(t.amount) FROM AccountTransaction t " +
           "WHERE t.status = 'POSTED' AND t.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY t.channel")
    List<Object[]> getTransactionStatsByChannel(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Reference-based lookup
    List<AccountTransaction> findByReference(String reference);

    // Related account transactions (for transfers)
    List<AccountTransaction> findByRelatedAccountId(String relatedAccountId);

    // User transaction history
    @Query("SELECT t FROM AccountTransaction t WHERE t.createdBy = :userId ORDER BY t.createdAt DESC")
    Page<AccountTransaction> findByCreatedByOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);
}
