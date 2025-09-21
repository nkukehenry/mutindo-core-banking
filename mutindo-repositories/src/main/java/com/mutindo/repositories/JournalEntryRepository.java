package com.mutindo.repositories;

import com.mutindo.entities.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Journal Entry repository - optimized for accounting operations
 */
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    // Idempotency check for posting engine
    Optional<JournalEntry> findByIdempotencyKey(String idempotencyKey);
    
    boolean existsByIdempotencyKey(String idempotencyKey);

    // Source transaction tracking
    Optional<JournalEntry> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
    
    List<JournalEntry> findBySourceType(String sourceType);

    // Date-based queries for reporting
    Page<JournalEntry> findByPostingDateOrderByCreatedAtDesc(LocalDate postingDate, Pageable pageable);

    @Query("SELECT j FROM JournalEntry j WHERE j.postingDate BETWEEN :startDate AND :endDate ORDER BY j.postingDate DESC, j.createdAt DESC")
    Page<JournalEntry> findByPostingDateBetweenOrderByPostingDateDesc(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // Branch-specific entries
    Page<JournalEntry> findByBranchIdAndPostingDateBetweenOrderByPostingDateDesc(
            Long branchId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Non-reversed entries for trial balance
    @Query("SELECT j FROM JournalEntry j WHERE j.reversed = false AND j.postingDate BETWEEN :startDate AND :endDate")
    List<JournalEntry> findNonReversedEntriesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Reversed entries
    List<JournalEntry> findByReversedTrue();
    
    List<JournalEntry> findByReversalEntryId(Long reversalEntryId);

    // Search functionality
    @Query("SELECT j FROM JournalEntry j WHERE " +
           "(LOWER(j.narration) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(j.sourceId AS string) LIKE CONCAT('%', :search, '%'))")
    Page<JournalEntry> searchEntries(@Param("search") String search, Pageable pageable);

    // User activity tracking
    Page<JournalEntry> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);

    // Posting date validation
    @Query("SELECT COUNT(j) FROM JournalEntry j WHERE j.postingDate = :postingDate")
    long countByPostingDate(@Param("postingDate") LocalDate postingDate);

    // Monthly/yearly aggregations
    @Query("SELECT EXTRACT(MONTH FROM j.postingDate), EXTRACT(YEAR FROM j.postingDate), COUNT(j) " +
           "FROM JournalEntry j WHERE j.postingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY EXTRACT(YEAR FROM j.postingDate), EXTRACT(MONTH FROM j.postingDate) " +
           "ORDER BY EXTRACT(YEAR FROM j.postingDate), EXTRACT(MONTH FROM j.postingDate)")
    List<Object[]> getMonthlyEntryCounts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
