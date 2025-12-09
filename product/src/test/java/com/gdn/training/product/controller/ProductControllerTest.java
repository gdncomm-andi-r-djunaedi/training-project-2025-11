package com.gdn.training.product.controller;

import com.gdn.training.product.dto.ProductDetailResponse;
import com.gdn.training.product.exception.ProductNotFoundException;
import com.gdn.training.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        ProductDetailResponse dto = new ProductDetailResponse();
        dto.setProductId(productId);
        dto.setProductName("Test Product");
        dto.setPrice(100.0);
        dto.setDescription("Test Description");

        when(productService.viewDetailById(productId)).thenReturn(dto);

        ResponseEntity<ProductDetailResponse> response = productController.viewDetailById(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProductDetailResponse body = response.getBody();
        assertEquals(productId, body.getProductId());
        assertEquals("Test Product", body.getProductName());
    }

    @Test
    @DisplayName("viewDetailById should throw exception when product does not exist")
    void viewDetailById_NotFound() {
        String productId = "INVALID-SKU";
        when(productService.viewDetailById(productId)).thenThrow(new ProductNotFoundException(productId));

        assertThrows(ProductNotFoundException.class, () -> productController.viewDetailById(productId));
    }
}
