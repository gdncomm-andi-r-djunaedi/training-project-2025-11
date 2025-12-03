package com.blibli.gdn.cartService.client;

import com.blibli.gdn.cartService.client.dto.ProductDTO;
import com.blibli.gdn.cartService.client.dto.VariantDTO;
import com.blibli.gdn.cartService.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

    @Mock
    private ProductFeignClient productFeignClient;
    
    @Mock
    private CircuitBreakerFactory circuitBreakerFactory;
    
    @Mock
    private CircuitBreaker circuitBreaker;

    private ProductServiceClient productServiceClient;

    @BeforeEach
    void setUp() {
        when(circuitBreakerFactory.create(anyString())).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(invocation -> {
            Supplier<Object> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        
        productServiceClient = new ProductServiceClient(productFeignClient, circuitBreakerFactory);
    }

    @Test
    void getVariantBySku_Success() {
        String sku = "SKU-123";
        VariantDTO variant = VariantDTO.builder().sku(sku).build();
        ProductDTO product = ProductDTO.builder()
                .variants(List.of(variant))
                .build();

        
        VariantDTO result = productServiceClient.getVariantBySku(product, sku);

        assertNotNull(result);
        assertEquals(sku, result.getSku());
    }

    @Test
    void getVariantBySku_VariantNotFound() {
        String sku = "SKU-123";
        ProductDTO product = ProductDTO.builder()
                .variants(Collections.emptyList())
                .build();

        assertThrows(ProductNotFoundException.class, () -> productServiceClient.getVariantBySku(product, sku));
    }

    @Test
    void getVariantBySku_VariantsNull_ShouldThrowProductNotFoundException() {
        // This test verifies the fix for the NPE.
        // Even if we fixed ProductDTO to initialize the list, we want to ensure the client handles null gracefully
        // in case the DTO comes from somewhere else or is manually constructed with null.
        String sku = "SKU-123";
        ProductDTO product = ProductDTO.builder().build();
        product.setVariants(null); // Explicitly set to null to test the null check in client

        assertThrows(ProductNotFoundException.class, () -> productServiceClient.getVariantBySku(product, sku));
    }
    
    @Test
    void getVariantBySku_ProductDTO_DefaultInitialization() {
        // Verify that ProductDTO initializes variants list by default
        ProductDTO product = ProductDTO.builder().build();
        assertNotNull(product.getVariants());
        assertTrue(product.getVariants().isEmpty());
    }
}
