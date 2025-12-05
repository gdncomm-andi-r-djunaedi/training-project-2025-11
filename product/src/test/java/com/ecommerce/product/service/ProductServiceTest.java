package com.ecommerce.product.service;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchProducts_ShouldReturnAll_WhenQueryIsEmpty() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Product> result = productService.searchProducts("", Pageable.unpaged());

        assertEquals(page, result);
    }

    @Test
    void searchProducts_ShouldReturnFiltered_WhenQueryIsProvided() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.findByNameRegex(any(), any(Pageable.class))).thenReturn(page);

        Page<Product> result = productService.searchProducts("test", Pageable.unpaged());

        assertEquals(page, result);
    }

    @Test
    void getProduct_ShouldReturnProduct_WhenFound() {
        Product product = new Product();
        product.setId("1");
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        Product result = productService.getProduct("1");

        assertEquals(product, result);
    }

    @Test
    void getProduct_ShouldThrowException_WhenNotFound() {
        when(productRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProduct("1"));
    }
}
