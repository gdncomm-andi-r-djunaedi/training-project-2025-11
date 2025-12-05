package com.blibli.product.messaging;

import com.blibli.product.dto.ProductEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.enabled:true}")
    private boolean kafkaEnabled;

    @Value("${kafka.topic.product-events:product-events}")
    private String topic;

    public void sendProductEvent(ProductEvent event) {
        if (!kafkaEnabled) {
            log.debug("Kafka is disabled. Skipping event: {} - {}", event.getEventType(), event.getId());
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, event.getId(), message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Product event sent successfully: {} - {} to partition: {}", 
                            event.getEventType(), 
                            event.getId(),
                            result.getRecordMetadata().partition());
                } else {
                    String errorMsg = ex.getMessage();
                    if (errorMsg != null && errorMsg.contains("not present in metadata")) {
                        log.error("Kafka topic '{}' does not exist! Please create it first.", topic);
                        log.error("Run: kafka-topics --create --topic {} --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1", topic);
                        log.error("Or use the script: ./create-kafka-topic.sh");
                    }
                    log.error("Failed to send product event: {} - {}. Error: {}", 
                            event.getEventType(), 
                            event.getId(), 
                            errorMsg);
                }
            });
        } catch (Exception e) {
            log.warn("Error processing product event: {} - {}. Error: {}", 
                    event.getEventType(), 
                    event.getId(), 
                    e.getMessage());
        }
    }
}
