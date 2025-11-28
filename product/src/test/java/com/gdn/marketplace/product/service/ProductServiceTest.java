package com.gdn.marketplace.product.service;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

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
        Page<Product> page = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        Page<Product> result = productService.searchProducts("Test", Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
