package com.gdn.marketplace.product.service;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private com.gdn.marketplace.product.repository.ProductSearchRepository productSearchRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId("1");
        product.setName("Test Product");
        product.setPrice(100.0);
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.createProduct(product);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productSearchRepository, times(1)).save(any(com.gdn.marketplace.product.document.ProductDocument.class));
    }

    @Test
    void getProduct_Success() {
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        Product result = productService.getProduct("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
    }

    @Test
    void searchProducts_Success() {
        com.gdn.marketplace.product.document.ProductDocument doc = new com.gdn.marketplace.product.document.ProductDocument();
        doc.setId("1");
        doc.setName("Test Product");

        when(productSearchRepository.findByNameContaining(anyString())).thenReturn(Collections.singletonList(doc));

        java.util.List<com.gdn.marketplace.product.document.ProductDocument> result = productService
                .searchProducts("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
    }
}
