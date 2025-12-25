package com.kailash.product.service.impl;

import com.kailash.product.dto.ProductEvent;
import com.kailash.product.dto.ProductPayload;
import com.kailash.product.entity.Product;
import com.kailash.product.service.ProductEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProductEventProducerImpl implements ProductEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventProducerImpl.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public ProductEventProducerImpl(KafkaTemplate<String, Object> kafkaTemplate,
                                    @Value("${indexer.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void sendProductUpsert(Product product) {
        ProductPayload payload = toPayload(product);

        ProductEvent event = new ProductEvent();
        event.setEventType("PRODUCT_UPSERT");
        event.setProductId(product.getId());
        event.setPayload(payload);
        event.setTimestamp(Instant.now());

        kafkaTemplate.send(topic, product.getId(), event)
                .whenComplete((SendResult<String, Object> result, Throwable ex) -> {
                    if (ex == null) {
                        log.info("SUCCESS: Sent PRODUCT_UPSERT for {}", product.getId());
                    } else {
                        log.error("FAILED: PRODUCT_UPSERT for {} - {}", product.getId(), ex.getMessage());
                    }
                });
    }

    @Override
    public void sendProductDelete(String productId) {
        ProductEvent event = new ProductEvent();
        event.setEventType("PRODUCT_DELETE");
        event.setProductId(productId);
        event.setPayload(null);
        event.setTimestamp(Instant.now());

        kafkaTemplate.send(topic, productId, event)
                .whenComplete((SendResult<String, Object> result, Throwable ex) -> {
                    if (ex == null) {
                        log.info("SUCCESS: Sent PRODUCT_DELETE for {}", productId);
                    } else {
                        log.error("FAILED: PRODUCT_DELETE for {} - {}", productId, ex.getMessage());
                    }
                });
    }

    private ProductPayload toPayload(Product product) {
        ProductPayload p = new ProductPayload();
        p.setId(product.getId());
        p.setSku(product.getSku());
        p.setName(product.getName());
        p.setShortDescription(product.getShortDescription());
        p.setPrice(product.getPrice());
        p.setCreatedAt(product.getCreatedAt());
        return p;
    }
}
