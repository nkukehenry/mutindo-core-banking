package com.mutindo.repositories;

import com.mutindo.entities.GroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Group Member repository - optimized for group membership management
 */
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {

    // Basic membership queries
    List<GroupMember> findByGroupId(String groupId);
    
    Page<GroupMember> findByGroupId(String groupId, Pageable pageable);
    
    List<GroupMember> findByCustomerId(String customerId);

    // Active membership queries
    List<GroupMember> findByGroupIdAndMembershipStatus(String groupId, String membershipStatus);
    
    @Query("SELECT gm FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findActiveMembers(@Param("groupId") String groupId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.customerId = :customerId AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findActiveGroupsForCustomer(@Param("customerId") String customerId);

    // Unique membership validation
    Optional<GroupMember> findByGroupIdAndCustomerId(String groupId, String customerId);
    
    boolean existsByGroupIdAndCustomerId(String groupId, String customerId);

    // Role-based queries
    List<GroupMember> findByGroupIdAndMemberRole(String groupId, String memberRole);
    
    @Query("SELECT gm FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.memberRole IN ('LEADER', 'SECRETARY', 'TREASURER') AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findGroupOfficials(@Param("groupId") String groupId);

    // Member number queries
    Optional<GroupMember> findByGroupIdAndMemberNumber(String groupId, String memberNumber);
    
    boolean existsByGroupIdAndMemberNumber(String groupId, String memberNumber);

    // Date-based queries
    @Query("SELECT gm FROM GroupMember gm WHERE gm.joinDate BETWEEN :startDate AND :endDate")
    List<GroupMember> findByJoinDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.exitDate BETWEEN :startDate AND :endDate")
    List<GroupMember> findByExitDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Financial queries
    @Query("SELECT gm FROM GroupMember gm WHERE gm.totalContributions >= :minContributions AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findByMinimumContributions(@Param("minContributions") BigDecimal minContributions);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.outstandingLoans > 0 AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findMembersWithOutstandingLoans();

    @Query("SELECT gm FROM GroupMember gm WHERE gm.savingsBalance >= :minSavings AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findByMinimumSavings(@Param("minSavings") BigDecimal minSavings);

    // Guarantor queries
    @Query("SELECT gm FROM GroupMember gm WHERE (gm.guarantorLimit - gm.currentGuarantees) >= :requiredAmount AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findAvailableGuarantors(@Param("requiredAmount") BigDecimal requiredAmount);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.currentGuarantees > 0 AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findActiveGuarantors();

    // Share ownership queries
    @Query("SELECT gm FROM GroupMember gm WHERE gm.sharesOwned >= :minShares AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findByMinimumShares(@Param("minShares") Integer minShares);

    // Introduction tracking
    List<GroupMember> findByIntroducedBy(String introducedBy);

    // Approval tracking
    List<GroupMember> findByApprovedBy(String approvedBy);
    
    @Query("SELECT gm FROM GroupMember gm WHERE gm.approvalDate IS NULL AND gm.membershipStatus = 'ACTIVE'")
    List<GroupMember> findUnapprovedMembers();

    // Statistics and analytics
    @Query("SELECT gm.membershipStatus, COUNT(gm) FROM GroupMember gm WHERE gm.groupId = :groupId GROUP BY gm.membershipStatus")
    List<Object[]> getMembershipStatusStatistics(@Param("groupId") String groupId);

    @Query("SELECT gm.memberRole, COUNT(gm) FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.membershipStatus = 'ACTIVE' GROUP BY gm.memberRole")
    List<Object[]> getMemberRoleStatistics(@Param("groupId") String groupId);

    @Query("SELECT COUNT(gm), AVG(gm.totalContributions), SUM(gm.savingsBalance), SUM(gm.outstandingLoans) " +
           "FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.membershipStatus = 'ACTIVE'")
    Object[] getGroupFinancialSummary(@Param("groupId") String groupId);

    // Member performance
    @Query("SELECT gm FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.membershipStatus = 'ACTIVE' " +
           "ORDER BY gm.totalContributions DESC")
    List<GroupMember> findTopContributors(@Param("groupId") String groupId, Pageable pageable);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.membershipStatus = 'ACTIVE' " +
           "ORDER BY gm.savingsBalance DESC")
    List<GroupMember> findTopSavers(@Param("groupId") String groupId, Pageable pageable);

    // Bulk operations
    @Modifying
    @Query("UPDATE GroupMember gm SET gm.totalContributions = gm.totalContributions + :amount WHERE gm.id = :memberId")
    void addContribution(@Param("memberId") String memberId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE GroupMember gm SET gm.savingsBalance = :balance WHERE gm.id = :memberId")
    void updateSavingsBalance(@Param("memberId") String memberId, @Param("balance") BigDecimal balance);

    @Modifying
    @Query("UPDATE GroupMember gm SET gm.outstandingLoans = :amount WHERE gm.id = :memberId")
    void updateOutstandingLoans(@Param("memberId") String memberId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE GroupMember gm SET gm.currentGuarantees = :amount WHERE gm.id = :memberId")
    void updateCurrentGuarantees(@Param("memberId") String memberId, @Param("amount") BigDecimal amount);

    // Validation and counts
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.membershipStatus = 'ACTIVE'")
    long countActiveMembersByGroup(@Param("groupId") String groupId);

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.customerId = :customerId AND gm.membershipStatus = 'ACTIVE'")
    long countActiveGroupsForCustomer(@Param("customerId") String customerId);
}
