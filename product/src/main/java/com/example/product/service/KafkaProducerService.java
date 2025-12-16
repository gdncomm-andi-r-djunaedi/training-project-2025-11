package com.example.product.service;

import com.example.product.dto.ProductResponseDTO;

public interface KafkaProducerService {

    void publishProductCreated(ProductResponseDTO product);

    void publishProductUpdated(ProductResponseDTO product);

    void publishProductDeleted(long productId);
}
