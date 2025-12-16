package com.example.search.kafka;

import com.example.search.entity.ProductDocument;
import com.example.search.dto.ProductResponseDTO;

import com.example.search.event.ProductUpdateEvent;
import com.example.search.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductConsumer {

    private final ProductRepository productRepository;

    @KafkaListener(topics = "product-topic", groupId = "product-search-group")
    public void consume(ProductUpdateEvent event) {
        log.info("Consumed event: {}", event);

        if ("DELETE".equals(event.getEventType())) {
            productRepository.deleteByProductId(event.getProductId());
            log.info("Deleted product with id: {}", event.getProductId());
            return;
        }

        ProductResponseDTO productDto = event.getProduct();
        if (productDto == null) {
            log.warn("Received event {} without product data", event.getEventType());
            return;
        }

        ProductDocument doc = new ProductDocument();
        doc.setId(String.valueOf(productDto.getProductId()));
        doc.setProductId(productDto.getProductId());
        doc.setTitle(productDto.getTitle());
        doc.setDescription(productDto.getDescription());
        if (productDto.getPrice() != null) {
            doc.setPrice(productDto.getPrice().doubleValue());
        }
        doc.setImageUrl(productDto.getImageUrl());
        doc.setCategory(productDto.getCategory());
        doc.setMarkForDelete(productDto.getMarkForDelete() != null && productDto.getMarkForDelete());
        
        productRepository.save(doc);
        log.info("Indexed product with id: {}", productDto.getProductId());
    }
}

