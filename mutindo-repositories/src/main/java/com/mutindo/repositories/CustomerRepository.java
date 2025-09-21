package com.mutindo.repositories;

import com.mutindo.common.enums.CustomerType;
import com.mutindo.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Customer repository - focused only on data access
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNationalId(String nationalId);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    List<Customer> findByPrimaryBranchId(Long branchId);

    Page<Customer> findByPrimaryBranchId(Long branchId, Pageable pageable);

    List<Customer> findByCustomerType(CustomerType customerType);

    Page<Customer> findByCustomerType(CustomerType customerType, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.primaryBranchId = :branchId AND c.customerType = :type")
    Page<Customer> findByBranchAndType(@Param("branchId") Long branchId, 
                                      @Param("type") CustomerType type, 
                                      Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.active = true AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.nationalId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchActiveCustomers(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.active = true AND c.primaryBranchId = :branchId AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.nationalId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchActiveCustomersByBranch(@Param("branchId") Long branchId,
                                                @Param("search") String search, 
                                                Pageable pageable);

    boolean existsByNationalId(String nationalId);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.active = true AND c.primaryBranchId = :branchId")
    long countActiveCustomersByBranch(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.active = true AND c.customerType = :type")
    long countActiveCustomersByType(@Param("type") CustomerType type);
}
