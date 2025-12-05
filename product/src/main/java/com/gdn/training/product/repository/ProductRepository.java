package com.gdn.training.product.repository;

import com.gdn.training.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Query(value = "SELECT * FROM products p WHERE p.product_id = ?1", nativeQuery = true)
    Optional<Product> viewProductDetail(String product_id);

    @Query(value = "SELECT * FROM products p WHERE LOWER(CAST(p.product_name AS TEXT)) LIKE LOWER(CONCAT('%', :productName, '%'))", nativeQuery = true)
    Page<Product> searchByName(@Param("productName") String product_name, Pageable paging);

    @Query(value = """
            SELECT * FROM products p
            WHERE (NULLIF(?1, '') IS NULL OR p.product_id = ?1)
              AND (NULLIF(?2, '') IS NULL OR LOWER(p.product_name) LIKE LOWER(CONCAT('%', ?2, '%')))
            """, nativeQuery = true)
    Page<Product> findByFilters(String productId, String productName, Pageable pageable);
}
