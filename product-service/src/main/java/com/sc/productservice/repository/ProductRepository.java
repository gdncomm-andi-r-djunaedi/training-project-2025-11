package com.sc.productservice.repository;

import com.sc.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findByProductCode(String productCode);

    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    Product deleteProductByProductCode(String productCode);

}