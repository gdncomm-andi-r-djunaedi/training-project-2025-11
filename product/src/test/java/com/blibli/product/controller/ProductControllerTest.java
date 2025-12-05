package com.blibli.product.controller;

import com.blibli.product.dto.PageResponse;
import com.blibli.product.dto.ProductRequest;
import com.blibli.product.dto.ProductResponse;
import com.blibli.product.enums.CategoryType;
import com.blibli.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = {
        CacheAutoConfiguration.class,
        RedisAutoConfiguration.class
})
@ContextConfiguration(classes = {ProductControllerTest.TestCacheConfig.class})
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @TestConfiguration
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PRODUCT_ID = "product-123";
    private static final String SKU = "SKU-12453-76452";

    @Test
    @DisplayName("Should create product successfully")
    void createProductSuccess() throws Exception {

        ProductRequest request = ProductRequest.builder()
                .sku(SKU)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .category(CategoryType.ELECTRONIC)
                .stockQuantity(100)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(PRODUCT_ID)
                .sku(SKU)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category(CategoryType.ELECTRONIC)
                .stockQuantity(100)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(PRODUCT_ID))
                .andExpect(jsonPath("$.data.sku").value(SKU));

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when request validation fails")
    void createProductFailureValidationError() throws Exception {

        ProductRequest invalidRequest = ProductRequest.builder()
                .sku("") // Invalid: empty SKU
                .name("") // Invalid: empty name
                .price(new BigDecimal("-10")) // Invalid: negative price
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProductSuccess() throws Exception {

        ProductRequest request = ProductRequest.builder()
                .sku(SKU)
                .name("Updated Product")
                .price(new BigDecimal("149.99"))
                .category(CategoryType.FASHION)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(PRODUCT_ID)
                .sku(SKU)
                .name("Updated Product")
                .price(new BigDecimal("149.99"))
                .category(CategoryType.FASHION)
                .build();

        when(productService.updateProduct(eq(PRODUCT_ID), any(ProductRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/products/{id}", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Product"));

        verify(productService).updateProduct(eq(PRODUCT_ID), any(ProductRequest.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void getProductSuccess() throws Exception {

        ProductResponse response = ProductResponse.builder()
                .id(PRODUCT_ID)
                .sku(SKU)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category(CategoryType.ELECTRONIC)
                .build();

        when(productService.getProductById(PRODUCT_ID)).thenReturn(response);


        mockMvc.perform(get("/api/products/id/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(PRODUCT_ID));

        verify(productService).getProductById(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void getProductBySkuSuccess() throws Exception {

        ProductResponse response = ProductResponse.builder()
                .id(PRODUCT_ID)
                .sku(SKU)
                .name("Test Product")
                .build();

        when(productService.getProductBySku(SKU)).thenReturn(response);

        mockMvc.perform(get("/api/products/sku/{sku}", SKU))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value(SKU));

        verify(productService).getProductBySku(SKU);
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void getAllProductsSuccess() throws Exception {

        PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                .content(new ArrayList<>())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .first(true)
                .build();

        when(productService.getAllProducts(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/products/list")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(20));

        verify(productService).getAllProducts(0, 20);
    }

    @Test
    @DisplayName("Should get products by category")
    void getProductsByCategorySuccess() throws Exception {
        PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                .content(new ArrayList<>())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .first(true)
                .build();

        when(productService.getProductsByCategory(eq(CategoryType.ELECTRONIC), eq(0), eq(20)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/products/category/{category}", "ELECTRONIC")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(productService).getProductsByCategory(eq(CategoryType.ELECTRONIC), eq(0), eq(20));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProductSuccess() throws Exception {
// given
        doNothing().when(productService).deleteProduct(PRODUCT_ID);

//     when and then
        mockMvc.perform(delete("/api/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(productService).deleteProduct(PRODUCT_ID);
    }
}

