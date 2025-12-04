package com.gdn.faurihakim;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findByProductName(String productName);

    Optional<Product> findByProductId(String productId);
}
