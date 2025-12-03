package com.blibli.search.kafka;

import com.blibli.search.services.ElasticsearchSearchService;
//import com.blibli.search.services.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

//    private final SearchService searchService;
    private final ElasticsearchSearchService elasticsearchSearchService;

    @KafkaListener(topics = "product-events")
    public void consumeProductEvent(String message) {
        log.info("========== Kafka message received ==========");
        log.info("Raw message: {}", message);
        
        try {
            // Parse JSON string to Map
            Map<String, Object> eventData = parseJsonToMap(message);
            String eventType = (String) eventData.get("eventType");
            String productId = (String) eventData.get("id");
            
            log.info("Event type: {}, Product ID: {}", eventType, productId);
            log.info("Event data keys: {}", eventData.keySet());
            log.info("Full event data: {}", eventData);

            if ("CREATE".equals(eventType) || "UPDATE".equals(eventType)) {
                log.info("Processing CREATE/UPDATE event for product: {}", productId);
                // Index to both Solr and Elasticsearch
//                try {
//                    searchService.indexProduct(eventData);
//                    log.info("Product indexed successfully in Solr: {}", eventData.get("id"));
//                } catch (Exception e) {
//                    log.error("Failed to index product in Solr: {}", e.getMessage(), e);
//                }
                
                try {
                    elasticsearchSearchService.indexProduct(eventData);
                    log.info("✅ Product indexed successfully in Elasticsearch: {}", productId);
                } catch (Exception e) {
                    log.error("❌ Failed to index product in Elasticsearch: {}", productId, e);
                    log.error("Error details: {}", e.getMessage(), e);
                }
            } else if ("DELETE".equals(eventType)) {
                log.info("Processing DELETE event for product: {}", productId);
                // Delete from both Solr and Elasticsearch
//                try {
//                    searchService.deleteProduct((String) eventData.get("id"));
//                    log.info("Product deleted from Solr: {}", eventData.get("id"));
//                } catch (Exception e) {
//                    log.error("Failed to delete product from Solr: {}", e.getMessage(), e);
//                }
                
                try {
                    elasticsearchSearchService.deleteProduct(productId);
                    log.info("✅ Product deleted from Elasticsearch: {}", productId);
                } catch (Exception e) {
                    log.error("❌ Failed to delete product from Elasticsearch: {}", productId, e);
                    log.error("Error details: {}", e.getMessage(), e);
                }
            } else {
                log.warn("⚠️ Unknown event type: {}", eventType);
            }
            log.info("========== Kafka message processing completed ==========");
        } catch (Exception e) {
            log.error("❌ Error processing Kafka message: {}", e.getMessage(), e);
            log.error("Message that failed: {}", message);
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            throw new RuntimeException("Failed to parse Kafka message", e);
        }
    }
}
