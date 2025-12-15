package com.gdn.marketplace.product.service;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    @Test
    void saveProduct() {
        Product product = new Product("1", "Phone", "Smartphone", new BigDecimal("1000"), "Electronics");
        when(repository.save(any(Product.class))).thenReturn(product);

        Product savedProduct = service.saveProduct(product);
        assertNotNull(savedProduct);
        assertEquals("Phone", savedProduct.getName());
    }

    @Test
    void getProductById() {
        Product product = new Product("1", "Phone", "Smartphone", new BigDecimal("1000"), "Electronics");
        when(repository.findById("1")).thenReturn(Optional.of(product));

        Product foundProduct = service.getProductById("1");
        assertNotNull(foundProduct);
        assertEquals("1", foundProduct.getId());
    }

    @Test
    void searchProducts() {
        Product product = new Product("1", "Phone", "Smartphone", new BigDecimal("1000"), "Electronics");
        Page<Product> page = new PageImpl<>(Collections.singletonList(product));
        when(repository.findByNameContainingIgnoreCase("Phone", PageRequest.of(0, 10))).thenReturn(page);

        Page<Product> result = service.searchProducts("Phone", 0, 10);
        assertEquals(1, result.getTotalElements());
    }
}
