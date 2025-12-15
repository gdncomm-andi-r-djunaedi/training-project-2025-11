package com.gdn.faurihakim;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findByProductId(String productId);

    @Query("{ 'productName': ?0 }")
    Optional<Product> findByProductName(String productName);

    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);
}
