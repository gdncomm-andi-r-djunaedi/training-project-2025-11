package com.microservice.product.repository;

import com.microservice.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.isActive = TRUE AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:searchTerm IS NULL OR :searchTerm = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(p.category) = LOWER(:category)) AND " +
           "(:brand IS NULL OR :brand = '' OR LOWER(p.brand) = LOWER(:brand)) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:dangerousLevel IS NULL OR p.dangerousLevel = :dangerousLevel) AND " +
           "(:storeId IS NULL OR p.storeId = :storeId)")
    Page<Product> findByFilters(
            @Param("searchTerm") String searchTerm,
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("isActive") Boolean isActive,
            @Param("dangerousLevel") Integer dangerousLevel,
            @Param("storeId") Integer storeId,
            Pageable pageable);
}
