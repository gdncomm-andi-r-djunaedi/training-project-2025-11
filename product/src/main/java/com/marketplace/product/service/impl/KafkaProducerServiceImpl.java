package com.marketplace.product.service.impl;

import com.marketplace.product.entity.Product;
import com.marketplace.product.event.ProductUpdateEvent;
import com.marketplace.product.service.KafkaProducerService;
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

    @Value("${kafka.topic.product-updates}")
    private String topicName;

    @Override
    public void publishProductCreated(Product product) {
        ProductUpdateEvent event = ProductUpdateEvent.builder()
                .eventType("CREATE")
                .productId(product.getProductId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .build();
        kafkaTemplate.send(topicName, event);
        log.info("Published CREATE event for productId: {}", product.getProductId());
    }

    @Override
    public void publishProductUpdated(Product product) {
        ProductUpdateEvent event = ProductUpdateEvent.builder()
                .eventType("UPDATE")
                .productId(product.getProductId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .build();
        kafkaTemplate.send(topicName, event);
        log.info("Published UPDATE event for productId: {}", product.getProductId());
    }

    @Override
    public void publishProductDeleted(String productId) {
        ProductUpdateEvent event = ProductUpdateEvent.builder()
                .eventType("DELETE")
                .productId(productId)
                .build();
        kafkaTemplate.send(topicName, event);
        log.info("Published DELETE event for productId: {}", productId);
    }
}
