package com.Product.ProductService.repository;

import com.Product.ProductService.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(
            String nameKeyword, String descriptionKeyword, Pageable pageable);
}
