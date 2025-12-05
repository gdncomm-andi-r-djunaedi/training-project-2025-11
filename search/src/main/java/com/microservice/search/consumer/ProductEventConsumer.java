package com.microservice.search.consumer;

import com.microservice.search.dto.ProductEventDto;
import com.microservice.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductSearchService productSearchService;

    @KafkaListener(topics = "com.service.product.events", groupId = "search-service-group")
    public void consumeProductEvent(
            @Payload ProductEventDto productEventDto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received product event: {} for SKU ID: {} from topic: {}", 
                    productEventDto.getEventType(), productEventDto.getSkuId(), topic);

            switch (productEventDto.getEventType()) {
                case CREATED:
                    handleProductCreated(productEventDto);
                    break;
                case UPDATED:
                    handleProductUpdated(productEventDto);
                    break;
                case DELETED:
                    handleProductDeleted(productEventDto);
                    break;
                default:
                    log.warn("Unknown event type: {} for SKU ID: {}", 
                            productEventDto.getEventType(), productEventDto.getSkuId());
            }

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            log.info("Successfully processed product event: {} for SKU ID: {}", 
                    productEventDto.getEventType(), productEventDto.getSkuId());

        } catch (Exception e) {
            log.error("Error processing product event: {} for SKU ID: {}", 
                    productEventDto.getEventType(), productEventDto.getSkuId(), e);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        }
    }

    private void handleProductCreated(ProductEventDto productEventDto) {
        log.info("Handling CREATED event for product with SKU ID: {}", productEventDto.getSkuId());
        productSearchService.indexProduct(productEventDto);
    }

    private void handleProductUpdated(ProductEventDto productEventDto) {
        log.info("Handling UPDATED event for product with SKU ID: {}", productEventDto.getSkuId());
        productSearchService.updateProduct(productEventDto);
    }

    private void handleProductDeleted(ProductEventDto productEventDto) {
        log.info("Handling DELETED event for product with SKU ID: {}", productEventDto.getSkuId());
        productSearchService.deleteProduct(productEventDto.getSkuId());
    }
}

