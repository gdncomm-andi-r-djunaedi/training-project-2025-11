package com.example.product.service.impl;

import com.example.product.dto.ProductResponseDTO;
import com.example.product.event.ProductUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceImplTest {

    @Mock
    private KafkaTemplate<String, ProductUpdateEvent> kafkaTemplate;

    @InjectMocks
    private KafkaProducerServiceImpl kafkaProducerService;

    private ProductResponseDTO productResponseDTO;
    private String topicName;
    private long productId;

    @BeforeEach
    void setUp() {
        topicName = "product-topic";
        productId = 1L;

        // Set the topic name using reflection since it's injected via @Value
        ReflectionTestUtils.setField(kafkaProducerService, "topicName", topicName);

        productResponseDTO = ProductResponseDTO.builder()
                .productId(productId)
                .title("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .imageUrl("test-image.jpg")
                .category("Electronics")
                .markForDelete(false)
                .build();
    }

    @Test
    void publishProductCreated_validProduct_sendsCreateEvent() {
        
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);

        kafkaProducerService.publishProductCreated(productResponseDTO);

        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());

        ProductUpdateEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("CREATE", capturedEvent.getEventType());
        assertEquals(productId, capturedEvent.getProductId());
        assertNotNull(capturedEvent.getProduct());
        assertEquals(productResponseDTO.getTitle(), capturedEvent.getProduct().getTitle());
        assertNotNull(capturedEvent.getTimestamp());
    }

    @Test
    void publishProductUpdated_validProduct_sendsUpdateEvent() {
        
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);

        kafkaProducerService.publishProductUpdated(productResponseDTO);

        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());

        ProductUpdateEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("UPDATE", capturedEvent.getEventType());
        assertEquals(productId, capturedEvent.getProductId());
        assertNotNull(capturedEvent.getProduct());
        assertEquals(productResponseDTO.getTitle(), capturedEvent.getProduct().getTitle());
        assertNotNull(capturedEvent.getTimestamp());
    }

    @Test
    void publishProductDeleted_validProductId_sendsDeleteEvent() {
        
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);
        
        kafkaProducerService.publishProductDeleted(productId);

        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());

        ProductUpdateEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("DELETE", capturedEvent.getEventType());
        assertEquals(productId, capturedEvent.getProductId());
        assertNull(capturedEvent.getProduct()); // Product should be null for DELETE events
        assertNotNull(capturedEvent.getTimestamp());
    }

    @Test
    void publishProductCreated_withNullFields_handlesGracefully() {
        
        ProductResponseDTO partialProduct = ProductResponseDTO.builder()
                .productId(productId)
                .title("Partial Product")
                .description(null)
                .price(null)
                .imageUrl(null)
                .category(null)
                .markForDelete(false)
                .build();

        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);

        
        kafkaProducerService.publishProductCreated(partialProduct);
        
        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());

        ProductUpdateEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("CREATE", capturedEvent.getEventType());
        assertNotNull(capturedEvent.getProduct());
        assertEquals("Partial Product", capturedEvent.getProduct().getTitle());
    }

    @Test
    void publishProductDeleted_withZeroId_sendsEvent() {
        
        long zeroId = 0L;
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);
        
        kafkaProducerService.publishProductDeleted(zeroId);
        
        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());

        ProductUpdateEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("DELETE", capturedEvent.getEventType());
        assertEquals(zeroId, capturedEvent.getProductId());
    }

    @Test
    void publishProductCreated_withMarkForDeleteTrue_sendsEvent() {
        
        productResponseDTO.setMarkForDelete(true);
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);

        kafkaProducerService.publishProductCreated(productResponseDTO);
        
        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());

        ProductUpdateEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertTrue(capturedEvent.getProduct().getMarkForDelete());
    }

    @ParameterizedTest
    @CsvSource({
            "CREATE, 1",
            "UPDATE, 2",
            "DELETE, 3"
    })
    void verifyEventType_allMethods_setsCorrectEventType(String expectedEventType, int productIdValue) {
        
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);
        ProductResponseDTO product = ProductResponseDTO.builder()
                .productId(productIdValue)
                .title("Test")
                .price(new BigDecimal("10.00"))
                .markForDelete(false)
                .build();
        
        switch (expectedEventType) {
            case "CREATE":
                kafkaProducerService.publishProductCreated(product);
                break;
            case "UPDATE":
                kafkaProducerService.publishProductUpdated(product);
                break;
            case "DELETE":
                kafkaProducerService.publishProductDeleted(productIdValue);
                break;
        }

        
        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());
        assertEquals(expectedEventType, eventCaptor.getValue().getEventType());
    }

    @Test
    void publishProductCreated_multipleProducts_sendsMultipleEvents() {
        
        ProductResponseDTO product1 = ProductResponseDTO.builder()
                .productId(1L)
                .title("Product 1")
                .price(new BigDecimal("10.00"))
                .markForDelete(false)
                .build();

        ProductResponseDTO product2 = ProductResponseDTO.builder()
                .productId(2L)
                .title("Product 2")
                .price(new BigDecimal("20.00"))
                .markForDelete(false)
                .build();

        ProductResponseDTO product3 = ProductResponseDTO.builder()
                .productId(3L)
                .title("Product 3")
                .price(new BigDecimal("30.00"))
                .markForDelete(false)
                .build();

        
        kafkaProducerService.publishProductCreated(product1);
        kafkaProducerService.publishProductCreated(product2);
        kafkaProducerService.publishProductCreated(product3);
        
        verify(kafkaTemplate, times(3)).send(eq(topicName), any(ProductUpdateEvent.class));
    }

    @Test
    void publishEvents_timestampGeneration_createsUniqueTimestamps() throws InterruptedException {
        
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);

        kafkaProducerService.publishProductCreated(productResponseDTO);
        Thread.sleep(10); // Small delay to ensure different timestamps
        kafkaProducerService.publishProductUpdated(productResponseDTO);
        
        verify(kafkaTemplate, times(2)).send(eq(topicName), eventCaptor.capture());

        Long firstTimestamp = eventCaptor.getAllValues().get(0).getTimestamp();
        Long secondTimestamp = eventCaptor.getAllValues().get(1).getTimestamp();

        assertNotNull(firstTimestamp);
        assertNotNull(secondTimestamp);
        assertTrue(secondTimestamp >= firstTimestamp);
    }

    @Test
    void publishProductCreated_usesCorrectTopic() {
        
        kafkaProducerService.publishProductCreated(productResponseDTO);

        verify(kafkaTemplate).send(eq(topicName), any(ProductUpdateEvent.class));
    }

    @Test
    void publishProductUpdated_usesCorrectTopic() {
        
        kafkaProducerService.publishProductUpdated(productResponseDTO);

        verify(kafkaTemplate).send(eq(topicName), any(ProductUpdateEvent.class));
    }

    @Test
    void publishProductDeleted_usesCorrectTopic() {
        
        kafkaProducerService.publishProductDeleted(productId);

        verify(kafkaTemplate).send(eq(topicName), any(ProductUpdateEvent.class));
    }

    @Test
    void publishProductDeleted_productIsNull_inDeleteEvent() {
        
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);
        
        kafkaProducerService.publishProductDeleted(productId);

        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());
        assertNull(eventCaptor.getValue().getProduct());
    }

    @Test
    void publishProductCreated_productIsNotNull_inCreateEvent() {
        
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);
        
        kafkaProducerService.publishProductCreated(productResponseDTO);
        
        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());
        assertNotNull(eventCaptor.getValue().getProduct());
        assertEquals(productResponseDTO, eventCaptor.getValue().getProduct());
    }

    @Test
    void publishProductUpdated_productIsNotNull_inUpdateEvent() {
        
        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);

        kafkaProducerService.publishProductUpdated(productResponseDTO);
        
        verify(kafkaTemplate).send(eq(topicName), eventCaptor.capture());
        assertNotNull(eventCaptor.getValue().getProduct());
        assertEquals(productResponseDTO, eventCaptor.getValue().getProduct());
    }
}
