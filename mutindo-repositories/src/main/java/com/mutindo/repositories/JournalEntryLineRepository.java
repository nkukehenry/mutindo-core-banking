package com.mutindo.repositories;

import com.mutindo.entities.JournalEntryLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Journal Entry Line repository - optimized for GL reporting and trial balance
 */
@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    // Lines for a specific journal entry
    List<JournalEntryLine> findByJournalEntryIdOrderByCreatedAt(Long journalEntryId);

    // GL Account balance calculations
    @Query("SELECT SUM(jel.debit - jel.credit) FROM JournalEntryLine jel " +
           "JOIN JournalEntry je ON jel.journalEntryId = je.id " +
           "WHERE jel.glAccountCode = :glAccountCode AND je.reversed = false " +
           "AND je.postingDate <= :asOfDate")
    BigDecimal calculateGLAccountBalance(
            @Param("glAccountCode") String glAccountCode,
            @Param("asOfDate") LocalDate asOfDate);

    // Trial balance data
    @Query("SELECT jel.glAccountCode, SUM(jel.debit), SUM(jel.credit) FROM JournalEntryLine jel " +
           "JOIN JournalEntry je ON jel.journalEntryId = je.id " +
           "WHERE je.reversed = false AND je.postingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY jel.glAccountCode ORDER BY jel.glAccountCode")
    List<Object[]> getTrialBalanceData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Branch-specific trial balance
    @Query("SELECT jel.glAccountCode, SUM(jel.debit), SUM(jel.credit) FROM JournalEntryLine jel " +
           "JOIN JournalEntry je ON jel.journalEntryId = je.id " +
           "WHERE je.reversed = false AND je.postingDate BETWEEN :startDate AND :endDate " +
           "AND jel.branchId = :branchId " +
           "GROUP BY jel.glAccountCode ORDER BY jel.glAccountCode")
    List<Object[]> getBranchTrialBalanceData(
            @Param("branchId") Long branchId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // GL Account activity
    @Query("SELECT jel FROM JournalEntryLine jel " +
           "JOIN JournalEntry je ON jel.journalEntryId = je.id " +
           "WHERE jel.glAccountCode = :glAccountCode AND je.reversed = false " +
           "AND je.postingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY je.postingDate DESC, je.createdAt DESC")
    Page<JournalEntryLine> findGLAccountActivity(
            @Param("glAccountCode") String glAccountCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // Currency-specific balances
    @Query("SELECT SUM(jel.debit - jel.credit) FROM JournalEntryLine jel " +
           "JOIN JournalEntry je ON jel.journalEntryId = je.id " +
           "WHERE jel.glAccountCode = :glAccountCode AND jel.currency = :currency " +
           "AND je.reversed = false AND je.postingDate <= :asOfDate")
    BigDecimal calculateGLAccountBalanceByCurrency(
            @Param("glAccountCode") String glAccountCode,
            @Param("currency") String currency,
            @Param("asOfDate") LocalDate asOfDate);

    // Balance validation - ensure debits = credits for each journal entry
    @Query("SELECT je.id, SUM(jel.debit), SUM(jel.credit) FROM JournalEntryLine jel " +
           "JOIN JournalEntry je ON jel.journalEntryId = je.id " +
           "WHERE je.postingDate = :postingDate " +
           "GROUP BY je.id " +
           "HAVING SUM(jel.debit) != SUM(jel.credit)")
    List<Object[]> findUnbalancedJournalEntries(@Param("postingDate") LocalDate postingDate);

    // Account code hierarchy queries
    @Query("SELECT jel.glAccountCode, SUM(jel.debit), SUM(jel.credit) FROM JournalEntryLine jel " +
           "JOIN JournalEntry je ON jel.journalEntryId = je.id " +
           "WHERE jel.glAccountCode LIKE CONCAT(:codePrefix, '%') " +
           "AND je.reversed = false AND je.postingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY jel.glAccountCode ORDER BY jel.glAccountCode")
    List<Object[]> getAccountHierarchyBalances(
            @Param("codePrefix") String codePrefix,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Branch activity summary
    @Query("SELECT jel.branchId, COUNT(jel), SUM(jel.debit), SUM(jel.credit) FROM JournalEntryLine jel " +
           "JOIN JournalEntry je ON jel.journalEntryId = je.id " +
           "WHERE je.reversed = false AND je.postingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY jel.branchId ORDER BY jel.branchId")
    List<Object[]> getBranchActivitySummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
