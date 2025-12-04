package com.gdn.training.product;

import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;
import com.gdn.training.product.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ViewProductDetailTest {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    @DisplayName("return valid product response test")
    public void returnValidProduct() {
        String productId = "SKU-000001";
        Product mockProduct = new Product();
        mockProduct.setProduct_id(productId);
        mockProduct.setProduct_name("Test Product");
        mockProduct.setPrice(new BigDecimal("10000"));

        when(productRepository.viewProductDetail(productId)).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = productService.viewDetailById(productId);

        assertTrue(result.isPresent());
        assertEquals(productId, result.get().getProduct_id());
        assertEquals("Test Product", result.get().getProduct_name());
        assertEquals(new BigDecimal("10000"), result.get().getPrice());
    }

    @Test
    @DisplayName("view product detail with invalid id should return empty optional")
    public void viewProductDetailWithInvalidIdTest() {
        String invalidProductId = "INVALID-ID";

        when(productRepository.viewProductDetail(invalidProductId)).thenReturn(Optional.empty());

        Optional<Product> result = productService.viewDetailById(invalidProductId);

        assertTrue(result.isEmpty());
    }
}
