package com.gdn.training.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceIntegrationTest {

    @SpyBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    private UUID gadgetId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        Product gadget = Product.builder()
                .name("Gadget Alpha")
                .description("Alpha gadget for testing")
                .price(BigDecimal.valueOf(199.99))
                .quantity(Integer.MAX_VALUE)
                .build();
        Product widget = Product.builder()
                .name("Widget Beta")
                .description("Widget for pagination")
                .price(BigDecimal.valueOf(49.99))
                .quantity(Integer.MAX_VALUE)
                .build();
        gadgetId = productRepository.save(gadget).getId();
        productRepository.save(widget);
    }

    @Test
    void searchProductsSupportsWildcardQueries() {
        Page<Product> results = productService.searchProducts("Gadget*", PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getName()).contains("Gadget");
    }

    @Test
    void getProductByIdCachesSubsequentCalls() {
        productService.getProductById(gadgetId);
        productService.getProductById(gadgetId);

        verify(productRepository, times(1)).findById(gadgetId);
    }
}

