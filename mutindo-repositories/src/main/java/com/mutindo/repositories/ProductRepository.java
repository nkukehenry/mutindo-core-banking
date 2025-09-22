package com.mutindo.repositories;

import com.mutindo.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product repository - focused only on data access
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    List<Product> findByProductType(String productType);

    List<Product> findByCategory(String category);

    List<Product> findByActive(Boolean active);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.productType = :productType")
    List<Product> findActiveByProductType(@Param("productType") String productType);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category = :category")
    List<Product> findActiveByCategory(@Param("category") String category);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Product> searchActiveProducts(@Param("search") String search);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();
}
