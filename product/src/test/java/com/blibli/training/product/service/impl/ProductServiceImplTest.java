package com.blibli.training.product.service.impl;

import com.blibli.training.product.dto.ProductRequest;
import com.blibli.training.product.dto.ProductResponse;
import com.blibli.training.product.entity.Product;
import com.blibli.training.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("testProduct")
                .price(100.0)
                .stock(10)
                .build();

        productRequest = ProductRequest.builder()
                .productName("testProduct")
                .price(100.0)
                .stock(10)
                .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals(product.getProductName(), response.getProductName());
        assertEquals(product.getPrice(), response.getPrice());
        assertEquals(product.getStock(), response.getStock());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void findByName_Success() {
        when(productRepository.findByProductName("testProduct")).thenReturn(Optional.of(product));

        ProductResponse response = productService.findByName("testProduct");

        assertNotNull(response);
        assertEquals(product.getProductName(), response.getProductName());
    }

    @Test
    void findByName_Fail_NotFound() {
        when(productRepository.findByProductName("testProduct")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.findByName("testProduct");
        });

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void getAllProduct_Success() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        when(productRepository.findAll()).thenReturn(products);

        List<ProductResponse> responses = productService.getAllProduct();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(product.getProductName(), responses.get(0).getProductName());
    }

    @Test
    void searchProduct_Success() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        org.springframework.data.domain.Page<Product> productPage = new org.springframework.data.domain.PageImpl<>(
                products);

        when(productRepository.findByProductNameLikeIgnoreCase(anyString(), any(PageRequest.class)))
                .thenReturn(productPage);

        List<ProductResponse> responses = productService.searchProduct("test", 0, 10);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(product.getProductName(), responses.get(0).getProductName());
    }
}
