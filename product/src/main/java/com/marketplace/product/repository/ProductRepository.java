package com.marketplace.product.repository;

import com.marketplace.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);

    Page<Product> findByBrandAndActiveTrue(String brand, Pageable pageable);

    @Query("{ 'active': true, '$or': [ " +
            "{ 'name': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'description': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'category': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'brand': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'tags': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    Page<Product> searchProducts(String keyword, Pageable pageable);

    @Query("{ 'active': true, '$text': { '$search': ?0 } }")
    Page<Product> fullTextSearch(String keyword, Pageable pageable);

    List<Product> findByIdIn(List<String> ids);

    long countByActiveTrue();
}

