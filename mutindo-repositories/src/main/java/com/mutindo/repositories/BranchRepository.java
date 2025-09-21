package com.mutindo.repositories;

import com.mutindo.entities.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Branch repository - focused only on data access
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByCode(String code);

    List<Branch> findByActiveTrue();

    Page<Branch> findByActiveTrue(Pageable pageable);

    @Query("SELECT b FROM Branch b WHERE b.active = true AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Branch> searchActiveBranches(@Param("search") String search, Pageable pageable);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(b) FROM Branch b WHERE b.active = true")
    long countActiveBranches();
}
