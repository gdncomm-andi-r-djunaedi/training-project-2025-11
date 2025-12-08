package com.blibi.product.repository;

import com.blibi.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

// Repository interface for Product data access operations.

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    java.util.Optional<Product> findByProductId(String productId);
    Page<Product> findByProductName(String productName, Pageable pageable);

    /**
     * Searches products by name using case-insensitive partial matching.
     * 
     * This method uses MongoDB's $regex operator with case-insensitive option ('i')
     * to perform flexible pattern matching. The query allows partial matches,
     * so searching for "laptop" will match "Laptop Pro", "Gaming Laptop", etc.
     * 
     * MongoDB Query Explanation:
     * - { 'productName': { $regex: ?0, $options: 'i' } }
     * - $regex: ?0 - Uses the first method parameter as the regex pattern
     * - $options: 'i' - Makes the regex case-insensitive
     */
    @Query("{ 'productName': { $regex: ?0, $options: 'i' } }")
    Page<Product> searchProductByName(String productName, Pageable pageable);

    @Query("{ 'category': { $regex: ?0, $options: 'i' } }")
    Page<Product> searchProductByCategory(String category, Pageable pageable);
}
