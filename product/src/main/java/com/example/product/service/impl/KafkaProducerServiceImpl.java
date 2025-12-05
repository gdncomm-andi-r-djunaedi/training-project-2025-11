package com.example.product.service.impl;

import com.example.product.dto.ProductResponseDTO;
import com.example.product.event.ProductUpdateEvent;
import com.example.product.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, ProductUpdateEvent> kafkaTemplate;

    @Value("${topic.product}")
    private String topicName;

    @Override
    public void publishProductCreated(ProductResponseDTO product) {
        ProductUpdateEvent event = ProductUpdateEvent.builder()
                .eventType("CREATE")
                .productId(product.getProductId())
                .product(product)
                .timestamp(System.currentTimeMillis())
                .build();
        kafkaTemplate.send(topicName, event);
        log.info("Published CREATE event for productId: {}", product.getProductId());
    }

    @Override
    public void publishProductUpdated(ProductResponseDTO product) {
        ProductUpdateEvent event = ProductUpdateEvent.builder()
                .eventType("UPDATE")
                .productId(product.getProductId())
                .product(product)
                .timestamp(System.currentTimeMillis())
                .build();
        kafkaTemplate.send(topicName, event);
        log.info("Published UPDATE event for productId: {}", product.getProductId());
    }

    @Override
    public void publishProductDeleted(long productId) {
        ProductUpdateEvent event = ProductUpdateEvent.builder()
                .eventType("DELETE")
                .productId(productId)
                .product(null)
                .timestamp(System.currentTimeMillis())
                .build();
        kafkaTemplate.send(topicName, event);
        log.info("Published DELETE event for productId: {}", productId);
    }
}
