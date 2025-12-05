package com.dev.onlineMarketplace.ProductService.repository;

import com.dev.onlineMarketplace.ProductService.model.ProductSku;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSkuRepository extends MongoRepository<ProductSku, String> {
    Optional<ProductSku> findBySku(String sku);

    List<ProductSku> findByProductId(String productId);
}
