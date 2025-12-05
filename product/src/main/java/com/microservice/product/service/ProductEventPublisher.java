package com.microservice.product.service;

import com.microservice.product.dto.ProductEventDto;
import com.microservice.product.dto.ProductResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Service to publish product events to Kafka
 */
@Service
@Slf4j
public class ProductEventPublisher {

    private static final String PRODUCT_EVENTS_TOPIC = "com.service.product.events";

    @Autowired
    private KafkaTemplate<String, ProductEventDto> kafkaTemplate;

    public void publishProductCreated(ProductResponseDto productResponseDto) {
        if (productResponseDto == null) {
            log.warn("Cannot publish CREATED event: product is null");
            return;
        }

        ProductEventDto event = createEvent(ProductEventDto.EventType.CREATED, productResponseDto);
        publishEvent(event, "CREATED");
    }

    public void publishProductUpdated(ProductResponseDto productResponseDto) {
        if (productResponseDto == null) {
            log.warn("Cannot publish UPDATED event: product is null");
            return;
        }

        ProductEventDto event = createEvent(ProductEventDto.EventType.UPDATED, productResponseDto);
        publishEvent(event, "UPDATED");
    }

    public void publishProductDeleted(String skuId) {
        if (skuId == null || skuId.isBlank()) {
            log.warn("Cannot publish DELETED event: SKU ID is null or empty");
            return;
        }

        ProductEventDto event = new ProductEventDto();
        event.setEventType(ProductEventDto.EventType.DELETED);
        event.setSkuId(skuId);
        event.setEventTimestamp(LocalDateTime.now());

        publishEvent(event, "DELETED");
    }

    private ProductEventDto createEvent(ProductEventDto.EventType eventType, ProductResponseDto productResponseDto) {
        ProductEventDto event = new ProductEventDto();
        event.setEventType(eventType);
        event.setSkuId(productResponseDto.getSkuId());
        event.setStoreId(productResponseDto.getStoreId());
        event.setName(productResponseDto.getName());
        event.setDescription(productResponseDto.getDescription());
        event.setCategory(productResponseDto.getCategory());
        event.setBrand(productResponseDto.getBrand());
        event.setPrice(productResponseDto.getPrice());
        event.setItemCode(productResponseDto.getItemCode());
        event.setIsActive(productResponseDto.getIsActive());
        event.setLength(productResponseDto.getLength());
        event.setHeight(productResponseDto.getHeight());
        event.setWidth(productResponseDto.getWidth());
        event.setWeight(productResponseDto.getWeight());
        event.setDangerousLevel(productResponseDto.getDangerousLevel());
        event.setCreatedAt(productResponseDto.getCreatedAt());
        event.setUpdatedAt(productResponseDto.getUpdatedAt());
        event.setEventTimestamp(LocalDateTime.now());

        return event;
    }

    private void publishEvent(ProductEventDto event, String eventTypeName) {
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate is not available. Skipping {} event for product SKU: {}",
                    eventTypeName, event.getSkuId());
            return;
        }
        try {
            log.info("Publishing {} event for product SKU: {}", eventTypeName, event.getSkuId());

            CompletableFuture<SendResult<String, ProductEventDto>> future =
                    kafkaTemplate.send(PRODUCT_EVENTS_TOPIC, event.getSkuId(), event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Successfully published {} event for product SKU: {} to topic: {}",
                            eventTypeName, event.getSkuId(), PRODUCT_EVENTS_TOPIC);
                } else {
                    log.error("Failed to publish {} event for product SKU: {} to topic: {}",
                            eventTypeName, event.getSkuId(), PRODUCT_EVENTS_TOPIC, exception);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing {} event for product SKU: {}",
                    eventTypeName, event.getSkuId(), e);
        }
    }
}





