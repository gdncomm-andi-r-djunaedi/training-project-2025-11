package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProductRepository extends MongoRepository<Product, String> {

    // Wildcard search on name
    @Query("{ 'name' : { $regex: ?0, $options: 'i' } }")
    Page<Product> findByNameRegex(String name, Pageable pageable);
}
