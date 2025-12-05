package com.blublu.product.controller;

import com.blublu.product.document.Products;
import com.blublu.product.interfaces.ProductService;
import com.blublu.product.model.response.GenericBodyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllProduct_Success() {
        Products product = Products.builder()
                .id("1")
                .skuCode("SKU123")
                .name("Test Product")
                .price(BigDecimal.TEN)
                .build();
        List<Products> mockProducts = Collections.singletonList(product);

        when(productService.findAllProductWithPageAndSize(anyInt(), anyInt())).thenReturn(mockProducts);

        ResponseEntity<?> response = productController.findAllProduct(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericBodyResponse body = (GenericBodyResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(mockProducts, body.getContent());
    }

    @Test
    void findAllProduct_NoContent() {
        when(productService.findAllProductWithPageAndSize(anyInt(), anyInt())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = productController.findAllProduct(0, 10);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse body = (GenericBodyResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("No product found", body.getErrorMessage());
    }

    @Test
    void findProductByName_Success() {
        Products product = Products.builder()
                .id("1")
                .name("Test Product")
                .build();
        List<Products> mockProducts = Collections.singletonList(product);

        when(productService.findByName(anyString(), anyInt(), anyInt())).thenReturn(mockProducts);

        ResponseEntity<?> response = productController.findProductByName("Test", 0, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericBodyResponse body = (GenericBodyResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        // The controller wraps the list in another list
        // (Collections.singletonList(products))
        assertEquals(Collections.singletonList(mockProducts), body.getContent());
    }

    @Test
    void findProductByName_NotFound() {
        when(productService.findByName(anyString(), anyInt(), anyInt())).thenReturn(null);

        ResponseEntity<?> response = productController.findProductByName("Test", 0, 5);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse body = (GenericBodyResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("No product found", body.getErrorMessage());
    }

    @Test
    void findProductDetail_Success() {
        Products product = Products.builder()
                .skuCode("SKU123")
                .name("Test Product")
                .build();

        when(productService.findProductBySkuCode(anyString())).thenReturn(product);

        ResponseEntity<?> response = productController.findProductDetail("SKU123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericBodyResponse body = (GenericBodyResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(Collections.singletonList(product), body.getContent());
    }

    @Test
    void findProductDetail_NotFound() {
        when(productService.findProductBySkuCode(anyString())).thenReturn(null);

        ResponseEntity<?> response = productController.findProductDetail("SKU123");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse body = (GenericBodyResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Product detail not found!", body.getErrorMessage());
    }
}
