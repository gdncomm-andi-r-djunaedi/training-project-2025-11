package com.marketplace.product.repository;

import com.marketplace.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    // Search by name containing (case-insensitive wildcard search)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Search by description containing
    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
    
    // Search by category
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // Full-text search using MongoDB text index
    @Query("{ $text: { $search: ?0 } }")
    Page<Product> fullTextSearch(String searchText, Pageable pageable);
    
    // Combined wildcard search (searches in both name and description)
    @Query("{ $or: [ " +
           "{ name: { $regex: ?0, $options: 'i' } }, " +
           "{ description: { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<Product> searchByNameOrDescription(String searchText, Pageable pageable);
}
