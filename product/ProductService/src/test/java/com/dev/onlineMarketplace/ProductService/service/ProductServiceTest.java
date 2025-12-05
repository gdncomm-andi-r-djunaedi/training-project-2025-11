package com.dev.onlineMarketplace.ProductService.service;

import com.dev.onlineMarketplace.ProductService.dto.ProductDTO;
import com.dev.onlineMarketplace.ProductService.dto.ProductSearchResponse;
import com.dev.onlineMarketplace.ProductService.model.Product;
import com.dev.onlineMarketplace.ProductService.repository.ProductRepository;
import com.dev.onlineMarketplace.ProductService.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("1", "iPhone 15", "SKU-IP15", "Apple Phone", 999.0, "Electronics", "img.jpg");
    }

    @Test
    void searchProducts_ShouldReturnProducts_WhenQueryIsProvided() {
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.searchByNameOrDescription(eq("iPhone"), any(Pageable.class)))
                .thenReturn(productPage);

        ProductSearchResponse response = productService.searchProducts("iPhone", 1, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals("iPhone 15", response.getItems().get(0).getName());
    }

    @Test
    void searchProducts_ShouldReturnAllProducts_WhenQueryIsEmpty() {
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponse response = productService.searchProducts("", 1, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
    }

    @Test
    void getProductByIdOrSku_ShouldReturnProduct_WhenFoundById() {
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductByIdOrSku("1");

        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
    }

    @Test
    void getProductByIdOrSku_ShouldReturnProduct_WhenFoundBySku() {
        when(productRepository.findById("SKU-IP15")).thenReturn(Optional.empty());
        when(productRepository.findBySku("SKU-IP15")).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductByIdOrSku("SKU-IP15");

        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        assertEquals("SKU-IP15", result.getSku());
    }

    @Test
    void getProductByIdOrSku_ShouldThrowException_WhenNotFound() {
        when(productRepository.findById("1")).thenReturn(Optional.empty());
        when(productRepository.findBySku("1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductByIdOrSku("1"));
    }
}
