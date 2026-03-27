package com.marketplace.search.consumer;

import com.marketplace.search.document.ProductDocument;
import com.marketplace.search.event.ProductUpdateEvent;
import com.marketplace.search.service.IndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUpdateConsumer {

    private final IndexService indexService;

    @KafkaListener(topics = "${kafka.topic.product-updates}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProductUpdate(ProductUpdateEvent event) {
        log.info("Received product update event: {} for productId: {}", event.getEventType(), event.getProductId());

        if ("DELETE".equals(event.getEventType())) {
            indexService.deleteProduct(event.getProductId());
        } else {
            ProductDocument document = ProductDocument.builder()
                    .productId(event.getProductId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .price(event.getPrice())
                    .imageUrl(event.getImageUrl())
                    .category(event.getCategory())
                    .build();
            indexService.saveProduct(document);
        }
    }
}
