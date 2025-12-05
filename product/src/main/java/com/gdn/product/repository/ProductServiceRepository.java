package com.gdn.product.repository;

import com.gdn.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductServiceRepository extends MongoRepository<Product,String> {
    Optional<Product> findByProductId(String productId);

    Page<Product> findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String nameKeyword,
            String descKeyword,
            Pageable pageable
    );

}
