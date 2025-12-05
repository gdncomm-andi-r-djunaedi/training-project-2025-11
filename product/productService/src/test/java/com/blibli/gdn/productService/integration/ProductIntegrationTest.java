package com.blibli.gdn.productService.integration;

import com.blibli.gdn.productService.dto.request.ProductRequest;
import com.blibli.gdn.productService.dto.request.VariantRequest;
import com.blibli.gdn.productService.dto.response.ProductResponse;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Product API endpoints
 * Tests the full flow from HTTP request to database
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        mongoTemplate.getDb().getCollection("products").drop();
    }

    @Test
    void createProduct_IntegrationTest() throws Exception {
        ProductRequest productRequest = ProductRequest.builder()
                .productId("INT-TEST-001")
                .name("Integration Test Product")
                .description("Integration test description")
                .category("Test Category")
                .brand("Test Brand")
                .tags(Collections.singletonList("test"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("INT-TEST-001-BLACK-M")
                                .price(99.99)
                                .stock(50)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/products")
                .header("X-User-Role", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Integration Test Product"))
                .andExpect(jsonPath("$.data.productId").value("INT-TEST-001"))
                .andExpect(jsonPath("$.success").value(true));

        // Verify product was saved in database
        assertTrue(productRepository.findFirstByProductId("INT-TEST-001").isPresent());
    }

    @Test
    void getProduct_IntegrationTest() throws Exception {
        // First create a product
        ProductRequest productRequest = ProductRequest.builder()
                .productId("INT-TEST-002")
                .name("Get Test Product")
                .description("Test description")
                .category("Test Category")
                .brand("Test Brand")
                .tags(Collections.singletonList("test"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("INT-TEST-002-BLACK-M")
                                .price(100.0)
                                .stock(10)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        ProductResponse created = productService.createProduct(productRequest);

        // Then retrieve it
        mockMvc.perform(get("/api/v1/products/" + created.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value("INT-TEST-002"))
                .andExpect(jsonPath("$.data.name").value("Get Test Product"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateProduct_IntegrationTest() throws Exception {
        // Create product
        ProductRequest createRequest = ProductRequest.builder()
                .productId("INT-TEST-003")
                .name("Original Name")
                .description("Original description")
                .category("Test Category")
                .brand("Test Brand")
                .tags(Collections.singletonList("test"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("INT-TEST-003-BLACK-M")
                                .price(100.0)
                                .stock(10)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        ProductResponse created = productService.createProduct(createRequest);
        String productId = created.getId();

        // Update product
        ProductRequest updateRequest = ProductRequest.builder()
                .productId("INT-TEST-003")
                .name("Updated Name")
                .description("Updated description")
                .category("Updated Category")
                .brand("Updated Brand")
                .tags(Collections.singletonList("updated"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("INT-TEST-003-BLACK-M")
                                .price(150.0)
                                .stock(20)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        mockMvc.perform(put("/api/v1/products/" + productId)
                .header("X-User-Role", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.success").value(true));

        // Verify update in database
        var updated = productService.getProduct("INT-TEST-003");
        assertEquals("Updated Name", updated.getName());
        assertEquals(150.0, updated.getVariants().get(0).getPrice());
    }

    @Test
    void deleteProduct_IntegrationTest() throws Exception {
        // Create product
        ProductRequest productRequest = ProductRequest.builder()
                .productId("INT-TEST-004")
                .name("Delete Test Product")
                .description("Test description")
                .category("Test Category")
                .brand("Test Brand")
                .tags(Collections.singletonList("test"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("INT-TEST-004-BLACK-M")
                                .price(100.0)
                                .stock(10)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        ProductResponse created = productService.createProduct(productRequest);
        String productId = created.getId();

        // Delete product
        mockMvc.perform(delete("/api/v1/products/" + productId)
                .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully"))
                .andExpect(jsonPath("$.success").value(true));

        // Verify deletion
        assertFalse(productRepository.findById(productId).isPresent());
    }

    @Test
    void searchProducts_IntegrationTest() throws Exception {
        // Create multiple products
        for (int i = 1; i <= 3; i++) {
            ProductRequest productRequest = ProductRequest.builder()
                    .productId("SEARCH-TEST-" + String.format("%03d", i))
                    .name("Search Test Product " + i)
                    .description("Test description " + i)
                    .category("Search Category")
                    .brand("Test Brand")
                    .tags(Collections.singletonList("search"))
                    .variants(Collections.singletonList(
                            VariantRequest.builder()
                                    .sku("SEARCH-TEST-" + String.format("%03d", i) + "-BLACK-M")
                                    .price(100.0 + i)
                                    .stock(10)
                                    .color("Black")
                                    .size("M")
                                    .build()
                    ))
                    .build();
            productService.createProduct(productRequest);
        }

        // Search products
        mockMvc.perform(get("/api/v1/products")
                .param("name", "Search Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createProduct_InvalidRequest_BadRequest() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .productId("") // Invalid: empty productId
                .name("Test")
                .build();

        mockMvc.perform(post("/api/v1/products")
                .header("X-User-Role", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProduct_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/NONEXISTENT"))
                .andExpect(status().isNotFound());
    }
}

