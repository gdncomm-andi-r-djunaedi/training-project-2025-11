package com.gdn.training.product.controller;

import com.gdn.training.product.entity.Product;
import com.gdn.training.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    @DisplayName("viewDetailById should return product details when product exists")
    void viewDetailById_Success() {
        String productId = "SKU-000001";
        Product product = new Product();
        product.setProduct_id(productId);
        product.setProduct_name("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setDescription("Test Description");

        when(productService.viewDetailById(productId)).thenReturn(Optional.of(product));

        ResponseEntity<Map<String, Object>> response = productController.viewDetailById(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertEquals("success", body.get("status"));
        assertEquals(productId, body.get("product_id"));
        assertEquals("Test Product", body.get("product_name"));
    }

    @Test
    @DisplayName("viewDetailById should return error message when product does not exist")
    void viewDetailById_NotFound() {
        String productId = "INVALID-SKU";
        when(productService.viewDetailById(productId)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = productController.viewDetailById(productId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("product not found", body.get("message"));
    }
}
