package com.mutindo.repositories;

import com.mutindo.entities.Group;
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
 * Group repository - optimized for group banking and SACCO operations
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, String> {

    // Basic lookups
    Optional<Group> findByGroupCode(String groupCode);
    
    boolean existsByGroupCode(String groupCode);

    // Branch-specific queries
    List<Group> findByBranchIdAndActiveTrue(String branchId);
    
    Page<Group> findByBranchIdAndActiveTrue(String branchId, Pageable pageable);

    // Group type queries
    List<Group> findByGroupTypeAndActiveTrue(String groupType);
    
    Page<Group> findByGroupTypeAndActiveTrue(String groupType, Pageable pageable);

    // Status-based queries
    List<Group> findByStatusAndActiveTrue(String status);
    
    Page<Group> findByStatusAndActiveTrue(String status, Pageable pageable);

    // Leadership queries
    List<Group> findByGroupLeaderIdAndActiveTrue(String groupLeaderId);
    
    List<Group> findBySecretaryIdAndActiveTrue(String secretaryId);
    
    List<Group> findByTreasurerIdAndActiveTrue(String treasurerId);

    // Search functionality
    @Query("SELECT g FROM Group g WHERE g.active = true AND " +
           "(LOWER(g.groupName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.groupCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.registrationNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Group> searchActiveGroups(@Param("search") String search, Pageable pageable);

    // Branch-specific search
    @Query("SELECT g FROM Group g WHERE g.branchId = :branchId AND g.active = true AND " +
           "(LOWER(g.groupName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.groupCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Group> searchActiveGroupsByBranch(
            @Param("branchId") String branchId,
            @Param("search") String search,
            Pageable pageable);

    // Member count queries
    @Query("SELECT g FROM Group g WHERE g.currentMembers >= :minMembers AND g.currentMembers <= :maxMembers AND g.active = true")
    List<Group> findByMemberCountRange(
            @Param("minMembers") Integer minMembers,
            @Param("maxMembers") Integer maxMembers);

    // Financial queries
    @Query("SELECT g FROM Group g WHERE g.totalSavings >= :minSavings AND g.active = true ORDER BY g.totalSavings DESC")
    List<Group> findByMinimumSavings(@Param("minSavings") BigDecimal minSavings);

    @Query("SELECT g FROM Group g WHERE g.totalLoans >= :minLoans AND g.active = true ORDER BY g.totalLoans DESC")
    List<Group> findByMinimumLoans(@Param("minLoans") BigDecimal minLoans);

    // Formation date queries
    @Query("SELECT g FROM Group g WHERE g.formationDate BETWEEN :startDate AND :endDate AND g.active = true")
    List<Group> findByFormationDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Meeting frequency queries
    List<Group> findByMeetingFrequencyAndActiveTrue(String meetingFrequency);

    // Registration queries
    @Query("SELECT g FROM Group g WHERE g.registrationNumber IS NOT NULL AND g.active = true")
    List<Group> findRegisteredGroups();

    @Query("SELECT g FROM Group g WHERE g.registrationNumber IS NULL AND g.active = true")
    List<Group> findUnregisteredGroups();

    // Statistics and analytics
    @Query("SELECT g.groupType, COUNT(g), AVG(g.currentMembers), SUM(g.totalSavings), SUM(g.totalLoans) " +
           "FROM Group g WHERE g.active = true GROUP BY g.groupType")
    List<Object[]> getGroupStatisticsByType();

    @Query("SELECT g.branchId, COUNT(g), SUM(g.currentMembers), SUM(g.totalSavings) " +
           "FROM Group g WHERE g.active = true GROUP BY g.branchId")
    List<Object[]> getGroupStatisticsByBranch();

    @Query("SELECT g.status, COUNT(g) FROM Group g WHERE g.active = true GROUP BY g.status")
    List<Object[]> getGroupCountByStatus();

    // Performance metrics
    @Query("SELECT g FROM Group g WHERE g.active = true AND g.status = 'ACTIVE' " +
           "ORDER BY (g.totalSavings / NULLIF(g.currentMembers, 0)) DESC")
    List<Group> findTopPerformingGroupsBySavingsPerMember(Pageable pageable);

    // Bulk operations
    @Modifying
    @Query("UPDATE Group g SET g.currentMembers = :memberCount WHERE g.id = :groupId")
    void updateMemberCount(@Param("groupId") String groupId, @Param("memberCount") Integer memberCount);

    @Modifying
    @Query("UPDATE Group g SET g.totalSavings = :totalSavings WHERE g.id = :groupId")
    void updateTotalSavings(@Param("groupId") String groupId, @Param("totalSavings") BigDecimal totalSavings);

    @Modifying
    @Query("UPDATE Group g SET g.totalLoans = :totalLoans WHERE g.id = :groupId")
    void updateTotalLoans(@Param("groupId") String groupId, @Param("totalLoans") BigDecimal totalLoans);

    // Validation queries
    @Query("SELECT COUNT(g) FROM Group g WHERE g.branchId = :branchId AND g.active = true")
    long countActiveGroupsByBranch(@Param("branchId") String branchId);

    boolean existsByRegistrationNumber(String registrationNumber);
}
