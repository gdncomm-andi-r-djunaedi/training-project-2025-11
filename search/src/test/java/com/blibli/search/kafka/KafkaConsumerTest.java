package com.blibli.search.kafka;

import com.blibli.search.services.ElasticsearchSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka Consumer Tests")

class KafkaConsumerTest {

    @Mock
    private ElasticsearchSearchService elasticsearchSearchService;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    private ObjectMapper objectMapper;
    private static final String PRODUCT_ID = "product-123";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }



    @Test
    @DisplayName("Should process CREATE event successfully")
    void consumeProductEvent_Success_CreateEvent() throws Exception {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "CREATE");
        eventData.put("id", PRODUCT_ID);
        eventData.put("name", "Test Product");
        eventData.put("price", 99.99);

        String message = objectMapper.writeValueAsString(eventData);
        doNothing().when(elasticsearchSearchService).indexProduct(any(Map.class));


        kafkaConsumer.consumeProductEvent(message);


        verify(elasticsearchSearchService).indexProduct(any(Map.class));
        verify(elasticsearchSearchService, never()).deleteProduct(anyString());
    }

    @Test
    @DisplayName("Should process UPDATE event successfully")
    void consumeProductEvent_Success_UpdateEvent() throws Exception {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "UPDATE");
        eventData.put("id", PRODUCT_ID);
        eventData.put("name", "Updated Product");
        eventData.put("price", 199.99);

        String message = objectMapper.writeValueAsString(eventData);
        doNothing().when(elasticsearchSearchService).indexProduct(any(Map.class));
        kafkaConsumer.consumeProductEvent(message);

        verify(elasticsearchSearchService).indexProduct(any(Map.class));
        verify(elasticsearchSearchService, never()).deleteProduct(anyString());
    }

    @Test
    @DisplayName("Should process DELETE event successfully")
    void consumeProductEvent_Success_DeleteEvent() throws Exception {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "DELETE");
        eventData.put("id", PRODUCT_ID);

        String message = objectMapper.writeValueAsString(eventData);
        doNothing().when(elasticsearchSearchService).deleteProduct(PRODUCT_ID);

        kafkaConsumer.consumeProductEvent(message);

        verify(elasticsearchSearchService).deleteProduct(PRODUCT_ID);
        verify(elasticsearchSearchService, never()).indexProduct(any(Map.class));
    }

    @Test
    @DisplayName("Should ignore unknown event type")
    void consumeProductEvent_Success_UnknownEventType() throws Exception {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "UNKNOWN");
        eventData.put("id", PRODUCT_ID);

        String message = objectMapper.writeValueAsString(eventData);


        kafkaConsumer.consumeProductEvent(message);

        verify(elasticsearchSearchService, never()).indexProduct(any(Map.class));
        verify(elasticsearchSearchService, never()).deleteProduct(anyString());
    }

    @Test
    @DisplayName("Should handle indexing failure gracefully")
    void consumeProductEvent_Success_IndexingFailure() throws Exception {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "CREATE");
        eventData.put("id", PRODUCT_ID);
        eventData.put("name", "Test Product");

        String message = objectMapper.writeValueAsString(eventData);
        doThrow(new RuntimeException("Elasticsearch error"))
                .when(elasticsearchSearchService).indexProduct(any(Map.class));

        kafkaConsumer.consumeProductEvent(message);

        verify(elasticsearchSearchService).indexProduct(any(Map.class));
    }

    @Test
    @DisplayName("Should handle deletion failure gracefully")
    void consumeProductEvent_Success_DeletionFailure() throws Exception {
        // Given
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "DELETE");
        eventData.put("id", PRODUCT_ID);

        String message = objectMapper.writeValueAsString(eventData);
        doThrow(new RuntimeException("Elasticsearch error"))
                .when(elasticsearchSearchService).deleteProduct(PRODUCT_ID);

        kafkaConsumer.consumeProductEvent(message);


        verify(elasticsearchSearchService).deleteProduct(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void consumeProductEvent_Success_InvalidJson() {

        String invalidJson = "{ invalid json }";

        kafkaConsumer.consumeProductEvent(invalidJson);

        verify(elasticsearchSearchService, never()).indexProduct(any(Map.class));
        verify(elasticsearchSearchService, never()).deleteProduct(anyString());
    }

    @Test
    @DisplayName("Should handle missing eventType field")
    void consumeProductEvent_Success_MissingEventType() throws Exception {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", PRODUCT_ID);
        eventData.put("name", "Test Product");

        String message = objectMapper.writeValueAsString(eventData);

        kafkaConsumer.consumeProductEvent(message);

        verify(elasticsearchSearchService, never()).indexProduct(any(Map.class));
        verify(elasticsearchSearchService, never()).deleteProduct(anyString());
    }

    @Test
    @DisplayName("Should handle missing id field")
    void consumeProductEvent_Success_MissingId() throws Exception {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "CREATE");
        eventData.put("name", "Test Product");

        String message = objectMapper.writeValueAsString(eventData);
        doNothing().when(elasticsearchSearchService).indexProduct(any(Map.class));

        kafkaConsumer.consumeProductEvent(message);

        verify(elasticsearchSearchService).indexProduct(any(Map.class));
    }

    @Test
    @DisplayName("Should handle empty message")
    void consumeProductEvent_Success_EmptyMessage() {

        String emptyMessage = "";

        kafkaConsumer.consumeProductEvent(emptyMessage);

        verify(elasticsearchSearchService, never()).indexProduct(any(Map.class));
        verify(elasticsearchSearchService, never()).deleteProduct(anyString());
    }

    @Test
    @DisplayName("Should handle null message")
    void consumeProductEvent_Success_NullMessage() {

        String nullMessage = null;

        kafkaConsumer.consumeProductEvent(nullMessage);

        verify(elasticsearchSearchService, never()).indexProduct(any(Map.class));
        verify(elasticsearchSearchService, never()).deleteProduct(anyString());
    }

    @Test
    @DisplayName("Should process event with all product fields")
    void consumeProductEvent_Success_AllFields() throws Exception {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "CREATE");
        eventData.put("id", PRODUCT_ID);
        eventData.put("name", "Test Product");
        eventData.put("description", "Test Description");
        eventData.put("price", 99.99);
        eventData.put("category", "ELECTRONIC");
        eventData.put("stockQuantity", 100);

        String message = objectMapper.writeValueAsString(eventData);
        doNothing().when(elasticsearchSearchService).indexProduct(any(Map.class));

        kafkaConsumer.consumeProductEvent(message);

        verify(elasticsearchSearchService).indexProduct(any(Map.class));
    }
}

