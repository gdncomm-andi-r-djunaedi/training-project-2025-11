package com.blibli.apigateway;

import com.blibli.apigateway.client.ProductClient;
import com.blibli.apigateway.controller.ProductController;
import com.blibli.apigateway.dto.response.PageResponse;
import com.blibli.apigateway.dto.request.ProductDto;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    private static final String TEST_PRODUCT_SERVICE_URL = "http://localhost:8008";
    private static final String TEST_PRODUCTS_LIST_ENDPOINT = "/api/products/list";

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        productController = new ProductController(productClient);
    }

    private Request createTestRequest() {
        return Request.create(
            Request.HttpMethod.GET,
            TEST_PRODUCT_SERVICE_URL + TEST_PRODUCTS_LIST_ENDPOINT,
            Collections.emptyMap(),
            new byte[0],
            StandardCharsets.UTF_8,
            null
        );
    }

    private static class TestFeignException extends FeignException {
        private final int statusCode;
        
        public TestFeignException(int status, String message, byte[] content) {
            super(status, message, (Throwable) null, content, (java.util.Map<String, java.util.Collection<String>>) null);
            this.statusCode = status;
        }
        
        @Override
        public int status() {
            return statusCode;
        }
    }

    @Test
    void testListProducts_Success() {
        List<ProductDto> products = new ArrayList<>();
        products.add(new ProductDto("PRD-00001", "Product 1", "Brand 1", "Description 1", 99.99, "http://image1.jpg", List.of("Category1")));
        products.add(new ProductDto("PRD-00002", "Product 2", "Brand 2", "Description 2", 149.99, "http://image2.jpg", List.of("Category2")));
        
        PageResponse<ProductDto> response = new PageResponse<>(
            products,
            0,
            20,
            2,
            1,
            true,
            true,
            2,
            false
        );

        when(productClient.listOfProducts(0, 20)).thenReturn(response);

        ResponseEntity<?> result = productController.listProducts(0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof PageResponse);
        
        @SuppressWarnings("unchecked")
        PageResponse<ProductDto> responseBody = (PageResponse<ProductDto>) result.getBody();
        assertEquals(2, responseBody.getTotalElements());
        assertEquals(1, responseBody.getTotalPages());
        assertEquals(2, responseBody.getContent().size());
        assertEquals("PRD-00001", responseBody.getContent().get(0).getProductCode());
    }

    @Test
    void testListProducts_WithPagination() {
        List<ProductDto> products = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            products.add(new ProductDto("PRD-" + String.format("%05d", i), "Product " + i, "Brand " + i, "Description " + i, 99.99 + i, "http://image" + i + ".jpg", List.of("Category")));
        }
        
        PageResponse<ProductDto> response = new PageResponse<>(
            products,
            0,
            20,
            50,
            3,
            true,
            false,
            20,
            false
        );

        when(productClient.listOfProducts(0, 20)).thenReturn(response);

        ResponseEntity<?> result = productController.listProducts(0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        @SuppressWarnings("unchecked")
        PageResponse<ProductDto> responseBody = (PageResponse<ProductDto>) result.getBody();
        assertEquals(50, responseBody.getTotalElements());
        assertEquals(3, responseBody.getTotalPages());
        assertEquals(20, responseBody.getContent().size());
    }

    @Test
    void testListProducts_EmptyResult() {
        PageResponse<ProductDto> response = new PageResponse<>(
            new ArrayList<>(),
            0,
            20,
            0,
            0,
            true,
            true,
            0,
            true
        );

        when(productClient.listOfProducts(0, 20)).thenReturn(response);

        ResponseEntity<?> result = productController.listProducts(0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        @SuppressWarnings("unchecked")
        PageResponse<ProductDto> responseBody = (PageResponse<ProductDto>) result.getBody();
        assertTrue(responseBody.isEmpty());
        assertEquals(0, responseBody.getTotalElements());
    }

    @Test
    void testListProducts_InvalidPagination_BadRequest() {
        String errorMessage = "Invalid pagination value: page must be >= 0 and size must be between 1 and 50";
        FeignException.BadRequest exception = new FeignException.BadRequest(
            "Bad Request",
            createTestRequest(),
            errorMessage.getBytes(StandardCharsets.UTF_8),
            null
        );

        when(productClient.listOfProducts(-1, 20)).thenThrow(exception);

        ResponseEntity<?> result = productController.listProducts(-1, 20);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Invalid pagination value"));
    }

    @Test
    void testListProducts_SizeExceedsLimit() {
        String errorMessage = "Invalid pagination value: size must be between 1 and 50";
        FeignException.BadRequest exception = new FeignException.BadRequest(
            "Bad Request",
            createTestRequest(),
            errorMessage.getBytes(StandardCharsets.UTF_8),
            null
        );

        when(productClient.listOfProducts(0, 100)).thenThrow(exception);

        ResponseEntity<?> result = productController.listProducts(0, 100);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().toString().contains("Invalid pagination value"));
    }

    @Test
    void testListProducts_ProductServiceUnavailable() {
        FeignException exception = new TestFeignException(
            503,
            "Service Unavailable",
            "Service unavailable".getBytes(StandardCharsets.UTF_8)
        );

        when(productClient.listOfProducts(anyInt(), anyInt())).thenThrow(exception);

        ResponseEntity<?> result = productController.listProducts(0, 20);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        String responseBody = (String) result.getBody();
        assertTrue(responseBody.contains("Service unavailable") || responseBody.contains("Error fetching products"));
    }

    @Test
    void testListProducts_DefaultParameters() {
        List<ProductDto> products = new ArrayList<>();
        products.add(new ProductDto("PRD-00001", "Product 1", "Brand 1", "Description 1", 99.99, "http://image1.jpg", List.of("Category1")));
        
        PageResponse<ProductDto> response = new PageResponse<>(
            products,
            0,
            20,
            1,
            1,
            true,
            true,
            1,
            false
        );

        when(productClient.listOfProducts(0, 20)).thenReturn(response);

        ResponseEntity<?> result = productController.listProducts(0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testSearchProducts_Success() {
        List<ProductDto> products = new ArrayList<>();
        products.add(new ProductDto("PRD-00001", "Laptop", "Brand 1", "Description 1", 999.99, "http://image1.jpg", List.of("Electronics")));
        products.add(new ProductDto("PRD-00002", "Laptop Bag", "Brand 2", "Description 2", 49.99, "http://image2.jpg", List.of("Accessories")));
        
        PageResponse<ProductDto> response = new PageResponse<>(
            products,
            0,
            20,
            2,
            1,
            true,
            true,
            2,
            false
        );

        when(productClient.searchProducts("laptop", 0, 20)).thenReturn(response);

        ResponseEntity<?> result = productController.searchProducts("laptop", 0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof PageResponse);
        
        @SuppressWarnings("unchecked")
        PageResponse<ProductDto> responseBody = (PageResponse<ProductDto>) result.getBody();
        assertEquals(2, responseBody.getTotalElements());
        assertEquals(2, responseBody.getContent().size());
        assertTrue(responseBody.getContent().get(0).getName().toLowerCase().contains("laptop") ||
                   responseBody.getContent().get(1).getName().toLowerCase().contains("laptop"));
    }

    @Test
    void testSearchProducts_NoResults() {
        PageResponse<ProductDto> response = new PageResponse<>(
            new ArrayList<>(),
            0,
            20,
            0,
            0,
            true,
            true,
            0,
            true
        );

        when(productClient.searchProducts("nonexistent", 0, 20)).thenReturn(response);

        ResponseEntity<?> result = productController.searchProducts("nonexistent", 0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        @SuppressWarnings("unchecked")
        PageResponse<ProductDto> responseBody = (PageResponse<ProductDto>) result.getBody();
        assertTrue(responseBody.isEmpty());
        assertEquals(0, responseBody.getTotalElements());
    }

    @Test
    void testSearchProducts_InvalidPagination_BadRequest() {
        String errorMessage = "Invalid pagination value: page must be >= 0 and size must be between 1 and 50";
        FeignException.BadRequest exception = new FeignException.BadRequest(
            "Bad Request",
            createTestRequest(),
            errorMessage.getBytes(StandardCharsets.UTF_8),
            null
        );

        when(productClient.searchProducts("laptop", -1, 20)).thenThrow(exception);

        ResponseEntity<?> result = productController.searchProducts("laptop", -1, 20);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Invalid pagination value"));
    }

    @Test
    void testSearchProducts_SizeExceedsLimit() {
        String errorMessage = "Invalid pagination value: size must be between 1 and 50";
        FeignException.BadRequest exception = new FeignException.BadRequest(
            "Bad Request",
            createTestRequest(),
            errorMessage.getBytes(StandardCharsets.UTF_8),
            null
        );

        when(productClient.searchProducts("laptop", 0, 100)).thenThrow(exception);

        ResponseEntity<?> result = productController.searchProducts("laptop", 0, 100);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().toString().contains("Invalid pagination value"));
    }

    @Test
    void testSearchProducts_ProductServiceUnavailable() {
        FeignException exception = new TestFeignException(
            503,
            "Service Unavailable",
            "Service unavailable".getBytes(StandardCharsets.UTF_8)
        );

        when(productClient.searchProducts(anyString(), anyInt(), anyInt())).thenThrow(exception);

        ResponseEntity<?> result = productController.searchProducts("laptop", 0, 20);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        String responseBody = (String) result.getBody();
        assertTrue(responseBody.contains("Service unavailable") || responseBody.contains("Error searching products"));
    }

    @Test
    void testGetProductDetails_Success() {
        ProductDto product = new ProductDto(
            "PRD-00001",
            "Laptop",
            "Brand 1",
            "High performance laptop",
            999.99,
            "http://image1.jpg",
            List.of("Electronics", "Computers")
        );

        when(productClient.getProductDetails("PRD-00001")).thenReturn(product);

        ResponseEntity<?> result = productController.getProductDetails("PRD-00001");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ProductDto);
        
        ProductDto responseBody = (ProductDto) result.getBody();
        assertEquals("PRD-00001", responseBody.getProductCode());
        assertEquals("Laptop", responseBody.getName());
        assertEquals(999.99, responseBody.getPrice());
        assertEquals("http://image1.jpg", responseBody.getImage());
        assertNotNull(responseBody.getCategory());
        assertEquals(2, responseBody.getCategory().size());
    }

    @Test
    void testGetProductDetails_ProductNotFound() {
        FeignException.NotFound exception = new FeignException.NotFound(
            "Not Found",
            createTestRequest(),
            "Product with code 'PRD-NOTFOUND' not found".getBytes(StandardCharsets.UTF_8),
            null
        );

        when(productClient.getProductDetails("PRD-NOTFOUND")).thenThrow(exception);

        ResponseEntity<?> result = productController.getProductDetails("PRD-NOTFOUND");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Product with code 'PRD-NOTFOUND' not found"));
    }

    @Test
    void testGetProductDetails_ProductNotFound_EmptyMessage() {
        FeignException.NotFound exception = new FeignException.NotFound(
            "Not Found",
            createTestRequest(),
            null,
            null
        );

        when(productClient.getProductDetails("PRD-NOTFOUND")).thenThrow(exception);

        ResponseEntity<?> result = productController.getProductDetails("PRD-NOTFOUND");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Product with code 'PRD-NOTFOUND' not found"));
    }

    @Test
    void testGetProductDetails_ProductServiceError() {
        FeignException exception = new TestFeignException(
            500,
            "Internal Server Error",
            "Internal server error".getBytes(StandardCharsets.UTF_8)
        );

        when(productClient.getProductDetails("PRD-00001")).thenThrow(exception);

        ResponseEntity<?> result = productController.getProductDetails("PRD-00001");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        String responseBody = (String) result.getBody();
        assertTrue(responseBody.contains("Internal server error") || responseBody.contains("Error fetching product"));
    }

    @Test
    void testGetProductDetails_ProductServiceUnavailable() {
        FeignException exception = new TestFeignException(
            503,
            "Service Unavailable",
            "Service unavailable".getBytes(StandardCharsets.UTF_8)
        );

        when(productClient.getProductDetails("PRD-00001")).thenThrow(exception);

        ResponseEntity<?> result = productController.getProductDetails("PRD-00001");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
    }
}

