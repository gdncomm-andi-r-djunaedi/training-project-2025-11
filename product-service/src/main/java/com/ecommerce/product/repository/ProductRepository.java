package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    @Query("{ $text: { $search: ?0 } }")
    Page<Product> searchByNameOrDescription(String keyword, Pageable pageable);

    Boolean existsBySku(String sku);
}
