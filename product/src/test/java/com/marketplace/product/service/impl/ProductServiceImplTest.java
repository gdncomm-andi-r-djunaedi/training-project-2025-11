package com.marketplace.product.service.impl;

import com.marketplace.product.dto.ProductRequest;
import com.marketplace.product.dto.ProductResponse;
import com.marketplace.product.entity.Product;
import com.marketplace.product.exception.ProductNotFoundException;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.service.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testCreateProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setTitle("Test Product");
        request.setDescription("Test Description");
        request.setPrice(BigDecimal.valueOf(100));
        request.setImageUrl("http://example.com/image.jpg");
        request.setCategory("Electronics");

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId("product-id");
            return product;
        });

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertNotNull(response.getProductId());
        assertEquals("Test Product", response.getTitle());
        verify(productRepository).save(any(Product.class));
        verify(kafkaProducerService).publishProductCreated(any(Product.class));
    }

    @Test
    void testGetProduct_Success() {
        String productId = "PROD-123";
        Product product = Product.builder()
                .productId(productId)
                .title("Test Product")
                .price(BigDecimal.valueOf(100))
                .build();

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(productId);

        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals("Test Product", response.getTitle());
    }

    @Test
    void testGetProduct_NotFound() {
        String productId = "NON-EXISTENT";

        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(productId));
    }
}

