package com.blibi.product.controller;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.blibi.product.utils.TestDataBuilder.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ProductController.
 *
 * This test class uses @WebMvcTest to test the controller layer in isolation.
 * MockMvc is used to simulate HTTP requests without starting a full server.
 *
 * Test Strategy:
 * - Mock ProductService
 * - Test HTTP endpoints and status codes
 * - Verify request/response JSON structure
 * - Test validation and error handling
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProductService productService;

        private ProductDTO testProductDTO;

        @BeforeEach
        void setUp() {
                testProductDTO = createTestProductDTO();
        }

        // ==================== POST /api/product Tests ====================

        @Test
        @DisplayName("createProduct - Valid Input - Returns 200 OK with Created Product")
        void createProduct_ValidInput_Returns200OK() throws Exception {
                // Arrange
                when(productService.createProduct(any(ProductDTO.class)))
                                .thenReturn(testProductDTO);

                // Act & Assert
                mockMvc.perform(post("/api/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testProductDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.message").value("Product created successfully"))
                                .andExpect(jsonPath("$.data.productId").value(testProductDTO.getProductId()))
                                .andExpect(jsonPath("$.data.productName").value(testProductDTO.getProductName()))
                                .andExpect(jsonPath("$.data.price").value(testProductDTO.getPrice()));

                verify(productService, times(1)).createProduct(any(ProductDTO.class));
        }

        @Test
        @DisplayName("createProduct - Missing Required Field - Returns 400 Bad Request")
        void createProduct_MissingRequiredField_Returns400BadRequest() throws Exception {
                // Arrange
                ProductDTO invalidDTO = new ProductDTO();
                invalidDTO.setProductId("MTA-000001");
                // Missing productName, description, price, category

                // Act & Assert
                mockMvc.perform(post("/api/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDTO)))
                                .andExpect(status().isBadRequest());

                verify(productService, never()).createProduct(any(ProductDTO.class));
        }
// ----- Product Creation with invalid Price and return 400

        @Test
        @DisplayName("createProduct - Invalid Price - Returns 400 Bad Request")
        void createProduct_InvalidPrice_Returns400BadRequest() throws Exception {
                // Arrange
                ProductDTO invalidDTO = createTestProductDTO();
                invalidDTO.setPrice(-10.0); // Negative price

                // Act & Assert
                mockMvc.perform(post("/api/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDTO)))
                                .andExpect(status().isBadRequest());

                verify(productService, never()).createProduct(any(ProductDTO.class));
        }

        // ==================== GET /api/product/name/{productName} Tests
        // ====================

        @Test
        @DisplayName("getProductByName - Products Found - Returns 200 OK with Products")
        void getProductByName_ProductsFound_Returns200OK() throws Exception {
                // Arrange
                Page<ProductDTO> productPage = createTestProductPage(PageRequest.of(0, 5))
                                .map(product -> createTestProductDTO());
                when(productService.viewProductDetailsByName(anyString(), any()))
                                .thenReturn(productPage);

                // Act & Assert
                mockMvc.perform(get("/api/product/name/Test Laptop")
                                .param("page", "0")
                                .param("size", "5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.content", hasSize(3)))
                                .andExpect(jsonPath("$.data.totalElements").value(3));

                verify(productService, times(1)).viewProductDetailsByName(anyString(), any());
        }

        // ==================== GET /api/product/search/{productName} Tests
        // ====================

        @Test
        @DisplayName("searchProductByName - Partial Match - Returns 200 OK with Matching Products")
        void searchProductByName_PartialMatch_Returns200OK() throws Exception {
                // Arrange
                Page<ProductDTO> productPage = createTestProductPage(PageRequest.of(0, 5))
                                .map(product -> createTestProductDTO());
                when(productService.searchProductByName(anyString(), any()))
                                .thenReturn(productPage);

                // Act & Assert
                mockMvc.perform(get("/api/product/search/Laptop")
                                .param("page", "0")
                                .param("size", "5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.totalElements").value(3));

                verify(productService, times(1)).searchProductByName(anyString(), any());
        }

        @Test
        @DisplayName("searchProductByName - Default Pagination - Uses Default Values")
        void searchProductByName_DefaultPagination_UsesDefaultValues() throws Exception {
                // Arrange
                Page<ProductDTO> productPage = createTestProductPage(PageRequest.of(0, 5))
                                .map(product -> createTestProductDTO());
                when(productService.searchProductByName(anyString(), any()))
                                .thenReturn(productPage);

                // Act & Assert
                mockMvc.perform(get("/api/product/search/Laptop"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.pageable.pageSize").value(5))
                                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0));

                verify(productService, times(1)).searchProductByName(anyString(), any());
        }

        // ==================== GET /api/product/category/{category} Tests
        // ====================

        @Test
        @DisplayName("searchProductByCategory - Category Found - Returns 200 OK with Products")
        void searchProductByCategory_CategoryFound_Returns200OK() throws Exception {
                // Arrange
                Page<ProductDTO> productPage = createTestProductPage(PageRequest.of(0, 5))
                                .map(product -> createTestProductDTO());
                when(productService.searchProductByCategory(anyString(), any()))
                                .thenReturn(productPage);

                // Act & Assert
                mockMvc.perform(get("/api/product/category/Electronics")
                                .param("page", "0")
                                .param("size", "5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.message").value("Products retrieved successfully"))
                                .andExpect(jsonPath("$.data.content").isArray());

                verify(productService, times(1)).searchProductByCategory(anyString(), any());
        }

        // ==================== GET /api/product/{id} Tests ====================

        @Test
        @DisplayName("getProductDetails - Product Found - Returns 200 OK with Product Details")
        void getProductDetails_ProductFound_Returns200OK() throws Exception {
                // Arrange
                when(productService.getProductDetail("MTA-000001"))
                                .thenReturn(testProductDTO);

                // Act & Assert
                mockMvc.perform(get("/api/product/MTA-000001"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.message").value("Product details retrieved successfully"))
                                .andExpect(jsonPath("$.data.productId").value("MTA-000001"))
                                .andExpect(jsonPath("$.data.productName").value(testProductDTO.getProductName()));

                verify(productService, times(1)).getProductDetail("MTA-000001");
        }

        @Test
        @DisplayName("getProductDetails - Product Not Found - Returns 200 OK with Error Message")
        void getProductDetails_ProductNotFound_Returns200WithError() throws Exception {
                // Arrange
                when(productService.getProductDetail("INVALID-ID"))
                                .thenThrow(new RuntimeException("Product not found with productId: INVALID-ID"));

                // Act & Assert
                mockMvc.perform(get("/api/product/INVALID-ID"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("ERROR"));

                verify(productService, times(1)).getProductDetail("INVALID-ID");
        }

        // ==================== Integration Tests ====================

        @Test
        @DisplayName("createProduct - Complete Flow - Verifies Full Request/Response Cycle")
        void createProduct_CompleteFlow_VerifiesFullCycle() throws Exception {
                // Arrange
                ProductDTO inputDTO = createTestProductDTO();
                inputDTO.setProductId(null); // New product, no ID yet

                ProductDTO createdDTO = createTestProductDTO();
                createdDTO.setProductId("MTA-000999");

                when(productService.createProduct(any(ProductDTO.class)))
                                .thenReturn(createdDTO);

                // Act & Assert
                mockMvc.perform(post("/api/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inputDTO)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.productId").value("MTA-000999"));

                verify(productService, times(1)).createProduct(any(ProductDTO.class));
        }
}
