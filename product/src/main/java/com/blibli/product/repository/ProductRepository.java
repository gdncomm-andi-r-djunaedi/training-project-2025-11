package com.blibli.product.repository;

import com.blibli.product.entity.Product;
import com.blibli.product.enums.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findAll(Pageable pageable);

    Optional<Product> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);

    Page<Product> findByIsActiveTrueAndCategory(CategoryType category, Pageable pageable);
}
