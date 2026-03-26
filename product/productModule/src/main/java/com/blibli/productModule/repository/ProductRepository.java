package com.blibli.productModule.repository;

import com.blibli.productModule.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String>, ProductRepositoryCustom {

    Optional<Product> findByProductId(String productId);

    boolean existsByProductId(String productId);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryAndIsActiveTrue(String category, Pageable pageable);

}
