package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigDecimal;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    // Text search using MongoDB's full-text search (RECOMMENDED - uses index)
    // Requires text index on name and description fields
    @Query("{ $text: { $search: ?0 } }")
    List<Product> searchByText(String searchTerm);

    // Case-insensitive exact match (uses name index)
    List<Product> findByNameIgnoreCase(String name);

    // Regex search (LEGACY - slower, but kept for backward compatibility)
    @Query("{ 'name' : { $regex: ?0, $options: 'i' } }")
    Page<Product> findByNameRegex(String name, Pageable pageable);

    // Find products within a price range (uses price index)
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
