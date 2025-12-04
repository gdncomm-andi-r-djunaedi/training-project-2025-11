package com.microservice.product.repository;

import com.microservice.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.isActive = TRUE AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<Product> findBySkuIdIn(List<String> skuIds);

    Optional<Product> findBySkuId(String skuId);

    Boolean existsBySkuId(String skuId);

    void deleteBySkuId(String skuId);
}