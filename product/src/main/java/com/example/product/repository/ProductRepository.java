package com.example.product.repository;

import com.example.product.entity.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
        extends MongoRepository<Product, ObjectId> {

    @Query("{ 'productName': { '$regex': '?0', '$options': 'i' } }")
    Page<Product> findByProductName(String productName, Pageable pageable);

    @Query("{ 'productName': { '$regex': '?0', '$options': 'i' }, 'category': '?1' }")
    Page<Product> findByProductNameAndCategory(String productName, String category, Pageable pageable);
}
