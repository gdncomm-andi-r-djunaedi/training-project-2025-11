package com.project.product.repository;

import com.project.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String>{
    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);

    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    @Query("{ $or: [ " +
            "{ 'name': { $regex: ?0, $options: 'i' } }, " +
            "{ 'description': { $regex: ?0, $options: 'i' } }, " +
            "{ 'category': { $regex: ?0, $options: 'i' } }, " +
            "{ 'tags': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<Product> searchProducts(String keyword, Pageable pageable);

    @Query("{ $and: [ " +
            "{ 'isActive': ?1 }, " +
            "{ $or: [ " +
            "{ 'name': { $regex: ?0, $options: 'i' } }, " +
            "{ 'description': { $regex: ?0, $options: 'i' } }, " +
            "{ 'category': { $regex: ?0, $options: 'i' } }, " +
            "{ 'tags': { $regex: ?0, $options: 'i' } } " +
            "] } " +
            "] }")
    Page<Product> searchActiveProducts(String keyword, Boolean isActive, Pageable pageable);
}
