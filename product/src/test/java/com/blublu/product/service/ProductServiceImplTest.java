package com.blublu.product.service;

import com.blublu.product.document.Products;
import com.blublu.product.repository.ProductsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductsRepository productsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllProductWithPageAndSize() {
        Products product = Products.builder().id("1").name("Test").build();
        Page<Products> page = new PageImpl<>(Collections.singletonList(product));

        when(productsRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<Products> result = productService.findAllProductWithPageAndSize(0, 10);

        assertEquals(1, result.size());
        assertEquals(product, result.get(0));
        verify(productsRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void findByName() {
        Products product = Products.builder().id("1").name("Test").build();
        List<Products> productList = Collections.singletonList(product);

        when(productsRepository.findProductsByName(anyString(), any(Pageable.class))).thenReturn(productList);

        List<Products> result = productService.findByName("Test", 0, 10);

        assertEquals(1, result.size());
        assertEquals(product, result.get(0));
        verify(productsRepository).findProductsByName("Test", PageRequest.of(0, 10));
    }

    @Test
    void findProductBySkuCode() {
        Products product = Products.builder().skuCode("SKU123").build();

        when(productsRepository.findBySkuCode(anyString())).thenReturn(product);

        Products result = productService.findProductBySkuCode("SKU123");

        assertEquals(product, result);
        verify(productsRepository).findBySkuCode("SKU123");
    }
}
