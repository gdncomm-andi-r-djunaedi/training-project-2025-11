package com.kailash.product.service;

import com.kailash.product.entity.Product;

public interface ProductEventProducer {
    void sendProductUpsert(Product product);
    void sendProductDelete(String productId);
}
