package com.example.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import com.example.product.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByNameContainingIgnoreCase(String q);
}
