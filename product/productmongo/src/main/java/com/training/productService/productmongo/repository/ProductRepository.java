package com.training.productService.productmongo.repository;

import com.training.productService.productmongo.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product,String>, ProductCustomRepository
{
    Optional<Object> findBySku(String sku);
}
