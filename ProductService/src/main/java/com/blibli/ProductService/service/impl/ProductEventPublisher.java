package com.blibli.ProductService.service.impl;

import com.blibli.ProductService.entity.Product;
import com.blibli.ProductService.event.ProductEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topics.product-events}")
    private String topic;

    public void sendProductCreated(Product product) {

        ProductEvent event = ProductEvent.builder()
                .eventType("PRODUCT_CREATED")
                .sku(product.getSku())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .eventTime(Instant.now())
                .build();

        log.info("Sending event to Kafka: {}", event);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            kafkaTemplate
                    .send(topic, event.getSku(), objectMapper.writeValueAsString(event))
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Kafka send failed", ex);
                        } else {
                            log.info("Message sent to topic {} offset {}",
                                    topic,
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("EVENT IS PRODUCED FOR TOPIC: {} and sku {}",topic,product.getSku());
    }
}