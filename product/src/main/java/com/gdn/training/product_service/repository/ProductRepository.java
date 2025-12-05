package com.gdn.training.product_service.repository;

import com.gdn.training.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * Search products by name (case-insensitive, wildcard)
     * Example: "laptop" matches "Gaming Laptop", "laptop bag", etc.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> searchByName(@Param("query") String query, Pageable pageable);

    /**
     * Find products by category (paginated)
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Find products by brand (paginated)
     */
    Page<Product> findByBrand(String brand, Pageable pageable);
}
