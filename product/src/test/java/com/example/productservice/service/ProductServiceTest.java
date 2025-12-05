package com.example.productservice.service;

import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProducts_shouldReturnPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(pageRequest)).thenReturn(page);

        Page<Product> result = productService.getProducts(0, 10);

        assertEquals(page, result);
    }

    @Test
    void getProduct_shouldReturnProduct_whenFound() {
        Product product = new Product();
        product.setId("1");
        when(repository.findById("1")).thenReturn(java.util.Optional.of(product));

        Product result = productService.getProduct("1");

        assertEquals(product, result);
    }

    @Test
    void getProduct_shouldThrowException_whenNotFound() {
        when(repository.findById("1")).thenReturn(java.util.Optional.empty());

        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> productService.getProduct("1"));
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void searchProducts_shouldReturnPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(repository.findByNameRegex("test", pageRequest)).thenReturn(page);

        Page<Product> result = productService.searchProducts("test", 0, 10);

        assertEquals(page, result);
    }

}
