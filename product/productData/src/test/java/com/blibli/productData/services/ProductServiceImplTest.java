package com.blibli.productData.services;


import com.blibli.productData.dto.ProductDTO;
import com.blibli.productData.entity.Product;
import com.blibli.productData.repositories.ProductRepository;
import com.blibli.productData.services.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    //When products found
    @Test
    void queryProducts_shouldReturnDTOPage() {

        String searchTerm = "phone";
        Pageable pageable = PageRequest.of(0, 10);

        Product p1 = new Product();
        p1.setName("Smartphone X");
        p1.setDescription("Latest phone");
        p1.setBrand("BrandA");
        p1.setPrice(500.0);

        Product p2 = new Product();
        p2.setName("Phone Case");
        p2.setDescription("Protective case");
        p2.setBrand("BrandB");
        p2.setPrice(20.0);

        List<Product> products = Arrays.asList(p1, p2);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        String pattern = ".*" + Pattern.quote(searchTerm) + ".*";
        when(productRepository.searchProducts(pattern, pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.queryProducts(searchTerm, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        List<String> names = result.map(ProductDTO::getName).getContent();
        assertTrue(names.contains("Smartphone X"));
        assertTrue(names.contains("Phone Case"));

        verify(productRepository, times(1)).searchProducts(pattern, pageable);
    }


    //wildcard + case-insensitive search
    @Test
    void queryProducts_shouldMatchWildcardAndBeCaseInsensitive() {
        String searchTerm = "phone";
        Pageable pageable = PageRequest.of(0, 10);

        Product p1 = new Product();
        p1.setName("Smartphone X");
        p1.setDescription("Latest Phone");
        p1.setBrand("BrandA");

        Product p2 = new Product();
        p2.setName("HeadPhones");
        p2.setDescription("Over-ear headphones");
        p2.setBrand("BrandB");

        List<Product> products = Arrays.asList(p1, p2);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        String pattern = ".*" + Pattern.quote(searchTerm) + ".*";
        when(productRepository.searchProducts(pattern, pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.queryProducts(searchTerm, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        List<String> names = result.map(ProductDTO::getName).getContent();
        assertTrue(names.contains("Smartphone X"));
        assertTrue(names.contains("HeadPhones"));

        verify(productRepository, times(1)).searchProducts(pattern, pageable);
    }


    //No products found
    @Test
    void queryProducts_shouldReturnEmptyPage_whenNoProductsFound() {
        String searchTerm = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        String pattern = ".*" + Pattern.quote(searchTerm) + ".*";
        when(productRepository.searchProducts(pattern, pageable)).thenReturn(emptyPage);

        Page<ProductDTO> result = productService.queryProducts(searchTerm, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(productRepository, times(1)).searchProducts(pattern, pageable);
    }
}