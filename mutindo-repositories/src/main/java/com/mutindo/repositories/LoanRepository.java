package com.mutindo.repositories;

import com.mutindo.entities.Loan;
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
 * Loan repository - optimized for loan management and portfolio analysis
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Customer loan queries
    List<Loan> findByCustomerId(String customerId);
    
    Page<Loan> findByCustomerId(String customerId, Pageable pageable);
    
    List<Loan> findByCustomerIdAndStatus(String customerId, String status);

    // Branch-specific queries
    List<Loan> findByBranchId(String branchId);
    
    Page<Loan> findByBranchId(String branchId, Pageable pageable);
    
    Page<Loan> findByBranchIdAndStatus(String branchId, String status, Pageable pageable);

    // Product-based queries
    List<Loan> findByProductCode(String productCode);
    
    Page<Loan> findByProductCodeAndStatus(String productCode, String status, Pageable pageable);

    // Status-based queries
    List<Loan> findByStatus(String status);
    
    Page<Loan> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    // Group loan queries
    List<Loan> findByGroupId(String groupId);
    
    @Query("SELECT l FROM Loan l WHERE l.groupId IS NOT NULL AND l.status IN ('ACTIVE', 'DISBURSED')")
    List<Loan> findActiveGroupLoans();

    // Arrears and overdue loans
    @Query("SELECT l FROM Loan l WHERE l.daysInArrears > :days AND l.status = 'ACTIVE'")
    List<Loan> findLoansInArrears(@Param("days") Integer days);

    @Query("SELECT l FROM Loan l WHERE l.daysInArrears > 0 AND l.status = 'ACTIVE' ORDER BY l.daysInArrears DESC")
    Page<Loan> findOverdueLoans(Pageable pageable);

    // Maturity date queries
    @Query("SELECT l FROM Loan l WHERE l.maturityDate <= :date AND l.status = 'ACTIVE'")
    List<Loan> findLoansMaturingBy(@Param("date") LocalDate date);

    @Query("SELECT l FROM Loan l WHERE l.maturityDate BETWEEN :startDate AND :endDate AND l.status = 'ACTIVE'")
    List<Loan> findLoansMaturingBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Disbursement queries
    @Query("SELECT l FROM Loan l WHERE l.disbursementDate BETWEEN :startDate AND :endDate")
    List<Loan> findLoansDisbursedBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT l FROM Loan l WHERE l.status = 'APPROVED' AND l.disbursementDate IS NULL")
    List<Loan> findApprovedLoansAwaitingDisbursement();

    // Outstanding amount queries
    @Query("SELECT l FROM Loan l WHERE l.outstandingPrincipal > :amount AND l.status = 'ACTIVE'")
    List<Loan> findLoansWithOutstandingPrincipalGreaterThan(@Param("amount") BigDecimal amount);

    // Portfolio analysis
    @Query("SELECT l.productCode, COUNT(l), SUM(l.principal), SUM(l.outstandingPrincipal) " +
           "FROM Loan l WHERE l.status = 'ACTIVE' GROUP BY l.productCode")
    List<Object[]> getLoanPortfolioByProduct();

    @Query("SELECT l.branchId, COUNT(l), SUM(l.principal), SUM(l.outstandingPrincipal) " +
           "FROM Loan l WHERE l.status = 'ACTIVE' GROUP BY l.branchId")
    List<Object[]> getLoanPortfolioByBranch();

    @Query("SELECT l.status, COUNT(l), SUM(l.principal) FROM Loan l GROUP BY l.status")
    List<Object[]> getLoanCountByStatus();

    // Risk analysis
    @Query("SELECT l FROM Loan l WHERE l.daysInArrears BETWEEN :minDays AND :maxDays AND l.status = 'ACTIVE'")
    List<Loan> findLoansByArrearsRange(
            @Param("minDays") Integer minDays,
            @Param("maxDays") Integer maxDays);

    @Query("SELECT COUNT(l), SUM(l.outstandingPrincipal) FROM Loan l " +
           "WHERE l.daysInArrears > :days AND l.status = 'ACTIVE'")
    Object[] getArrearsStatistics(@Param("days") Integer days);

    // Performance metrics
    @Query("SELECT AVG(l.outstandingPrincipal), MAX(l.outstandingPrincipal), MIN(l.outstandingPrincipal) " +
           "FROM Loan l WHERE l.status = 'ACTIVE'")
    Object[] getOutstandingPrincipalStatistics();

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' ORDER BY l.outstandingPrincipal DESC")
    List<Loan> findLargestLoans(Pageable pageable);

    // Search functionality
    @Query("SELECT l FROM Loan l WHERE l.customerId IN " +
           "(SELECT CAST(c.id AS string) FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           "l.loanAccountId LIKE CONCAT('%', :search, '%')")
    Page<Loan> searchLoans(@Param("search") String search, Pageable pageable);

    // Validation queries
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.customerId = :customerId AND l.status IN ('ACTIVE', 'DISBURSED')")
    long countActiveLoansForCustomer(@Param("customerId") String customerId);

    @Query("SELECT SUM(l.outstandingPrincipal) FROM Loan l WHERE l.customerId = :customerId AND l.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingForCustomer(@Param("customerId") String customerId);
}
