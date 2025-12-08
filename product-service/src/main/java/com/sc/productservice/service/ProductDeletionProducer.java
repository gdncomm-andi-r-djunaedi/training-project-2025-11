package com.sc.productservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductDeletionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.product-deleted}")
    private String topic;

    public ProductDeletionProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendProductDeletedEvent(String productCode) {
        Map<String, String> message = new HashMap<>();
        message.put("productCode", productCode);

        kafkaTemplate.send(topic, message);
        System.out.println("Product deleted event sent for productCode: " + productCode);
    }
}

