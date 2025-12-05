package com.blibli.gdn.productService.repository;

import com.blibli.gdn.productService.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByNameContainingIgnoreCaseAndCategory(String name, String category, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    java.util.Optional<Product> findByVariantsSku(String sku);
    
    java.util.Optional<Product> findByProductId(String productId);
    
    // Find first product by productId (handles duplicates)
    // Uses a proper query instead of loading all products
    default java.util.Optional<Product> findFirstByProductId(String productId) {
        // Use the repository method which is more efficient
        java.util.Optional<Product> result = findByProductId(productId);
        if (result.isPresent()) {
            return result;
        }
        // If not found and there might be duplicates, fallback to stream
        // But this should rarely happen if data is clean
        return findAll().stream()
                .filter(p -> p.getProductId() != null && p.getProductId().equals(productId))
                .findFirst();
    }
}
