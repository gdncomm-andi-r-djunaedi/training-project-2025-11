package com.marketplace.product.service;

import com.marketplace.product.entity.Product;

public interface KafkaProducerService {
    void publishProductCreated(Product product);
    void publishProductUpdated(Product product);
    void publishProductDeleted(String productId);
}
