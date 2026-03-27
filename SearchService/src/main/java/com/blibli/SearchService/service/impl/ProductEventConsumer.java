package com.blibli.SearchService.service.impl;


import com.blibli.SearchService.entity.ProductDocument;
import com.blibli.SearchService.event.ProductEvent;
import com.blibli.SearchService.repository.ProductSearchRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final ProductSearchRepository repository;

    @KafkaListener(
            topics = "${app.kafka.topics.product-events}",
            groupId = "search-service"
    )
    public void consume(String eventString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ProductEvent event = objectMapper.readValue(eventString, ProductEvent.class);
        if (!"PRODUCT_CREATED".equals(event.getEventType())) {
            return;
        }

        ProductDocument document = ProductDocument.builder()
                .sku(event.getSku())
                .productName(event.getProductName())
                .description(event.getDescription())
                .price(event.getPrice())
                .category(event.getCategory())
                .build();

        repository.save(document);
        log.info("Event is consumed for event {} and  for sku {}",event.getEventType(),event.getSku());
    }
}