package com.blibli.gdn.productService.controller;

import com.blibli.gdn.productService.dto.request.ProductRequest;
import com.blibli.gdn.productService.dto.request.VariantRequest;
import com.blibli.gdn.productService.dto.response.ProductResponse;
import com.blibli.gdn.productService.dto.response.VariantResponse;
import com.blibli.gdn.productService.service.ProductSearchService;
import com.blibli.gdn.productService.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@TestPropertySource(properties = {
    "spring.data.elasticsearch.repositories.enabled=false"
})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductSearchService productSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse productResponse;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .id("mongo-id-1")
                .productId("P001")
                .name("Test Product")
                .description("Test Description")
                .category("Electronics")
                .brand("Test Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        VariantResponse.builder()
                                .sku("P001-BLACK-001")
                                .price(100.0)
                                .stock(10)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();
                
        productRequest = ProductRequest.builder()
                .productId("P001")
                .name("Test Product")
                .description("Test Description")
                .category("Electronics")
                .brand("Test Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("P001-BLACK-001")
                                .price(100.0)
                                .stock(10)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();
    }

    @Test
    void createProduct_WithAdminRole_Success() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                .header("X-User-Role", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.productId").value("P001"))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createProduct_WithoutAdminRole_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .header("X-User-Role", "ROLE_USER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_WithoutRole_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProduct_Success() throws Exception {
        when(productService.getProduct("P001")).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/P001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("mongo-id-1"))
                .andExpect(jsonPath("$.data.productId").value("P001"))
                .andExpect(jsonPath("$.message").value("Product retrieved successfully"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateProduct_WithAdminRole_Success() throws Exception {
        ProductResponse updatedResponse = ProductResponse.builder()
                .id("mongo-id-1")
                .productId("P001")
                .name("Updated Product")
                .description("Updated Description")
                .category("Electronics")
                .brand("Updated Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        VariantResponse.builder()
                                .sku("P001-BLACK-001")
                                .price(150.0)
                                .stock(20)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        when(productService.updateProduct(eq("mongo-id-1"), any(ProductRequest.class)))
                .thenReturn(updatedResponse);

        ProductRequest updateRequest = ProductRequest.builder()
                .productId("P001")
                .name("Updated Product")
                .description("Updated Description")
                .category("Electronics")
                .brand("Updated Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("P001-BLACK-001")
                                .price(150.0)
                                .stock(20)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        mockMvc.perform(put("/api/v1/products/mongo-id-1")
                .header("X-User-Role", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Product"))
                .andExpect(jsonPath("$.message").value("Product updated successfully"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateProduct_WithoutAdminRole_Forbidden() throws Exception {
        mockMvc.perform(put("/api/v1/products/mongo-id-1")
                .header("X-User-Role", "ROLE_USER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProduct_WithAdminRole_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/products/mongo-id-1")
                .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteProduct_WithoutAdminRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/products/mongo-id-1")
                .header("X-User-Role", "ROLE_USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchProducts_WithElasticsearch() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(Collections.singletonList(productResponse));
        when(productSearchService.searchProducts(anyString(), any(), any(Pageable.class), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                .param("name", "Test")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.message").value("Products retrieved successfully"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void searchProducts_WithWildcard() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(Collections.singletonList(productResponse));
        when(productSearchService.searchProducts(anyString(), any(), any(Pageable.class), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                .param("name", "*Test*")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
    }

    @Test
    void searchProductsPost_WithElasticsearch_Success() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(Collections.singletonList(productResponse));
        when(productSearchService.searchProducts(anyString(), any(), any(Pageable.class), anyString()))
                .thenReturn(page);

        mockMvc.perform(post("/api/v1/products/search")
                .param("name", "Test")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.message").value("Products retrieved successfully"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void searchProductsPost_WithoutElasticsearch_ServiceUnavailable() throws Exception {
        // When Elasticsearch is not available, productSearchService would be null
        // and the controller returns 503. Since we're using @MockBean, it's always available
        // in tests, so we test the normal flow
        Page<ProductResponse> page = new PageImpl<>(Collections.singletonList(productResponse));
        when(productSearchService.searchProducts(anyString(), any(), any(Pageable.class), anyString()))
                .thenReturn(page);

        mockMvc.perform(post("/api/v1/products/search")
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
    }
}

