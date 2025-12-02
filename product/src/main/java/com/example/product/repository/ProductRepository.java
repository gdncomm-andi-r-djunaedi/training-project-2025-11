package com.example.product.repository;

import com.example.product.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    Optional<Product> findByProductId(long productId);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByTitleContainingIgnoreCase(String title);

    void deleteProductByProductId(long id);
}
