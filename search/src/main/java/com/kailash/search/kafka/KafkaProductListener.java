package com.kailash.search.kafka;

import com.kailash.search.dto.ProductEvent;
import com.kailash.search.service.ProductIndexerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class KafkaProductListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaProductListener.class);
    private final ProductIndexerService indexerService;

    public KafkaProductListener(ProductIndexerService indexerService) {
        this.indexerService = indexerService;
    }

    @KafkaListener(topics = "${indexer.kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ProductEvent event, Acknowledgment ack, ConsumerRecord<String, ProductEvent> record) {
        try {
            indexerService.handleEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process event for key {}", record.key(), e);

            ack.acknowledge();
        }
    }
}
