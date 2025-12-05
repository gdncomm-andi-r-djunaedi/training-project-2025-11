package com.blibli.product.repository;

import com.blibli.product.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product,String> {
    Product findByProductSku(String productSku);

    List<Product> findByProductSkuIn(List<String> productIds);
}
