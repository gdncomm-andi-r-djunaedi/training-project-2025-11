package com.marketplace.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.product.entity.Product;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductIntegrationTest {

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private static String testProductId;

    @BeforeAll
    static void beforeAll(@Autowired ProductRepository repository) {
        // Create test products
        Product product1 = Product.builder()
                .name("Test Laptop Pro")
                .description("High performance laptop for testing")
                .category("Electronics")
                .brand("TestBrand")
                .price(BigDecimal.valueOf(999.99))
                .active(true)
                .tags(List.of("electronics", "laptop", "pro"))
                .build();

        Product product2 = Product.builder()
                .name("Test Phone Max")
                .description("Premium smartphone for testing")
                .category("Electronics")
                .brand("TestBrand")
                .price(BigDecimal.valueOf(799.99))
                .active(true)
                .tags(List.of("electronics", "phone", "premium"))
                .build();

        Product product3 = Product.builder()
                .name("Test T-Shirt")
                .description("Comfortable cotton t-shirt")
                .category("Fashion")
                .brand("StyleTest")
                .price(BigDecimal.valueOf(29.99))
                .active(true)
                .tags(List.of("fashion", "clothing", "cotton"))
                .build();

        Product saved = repository.save(product1);
        testProductId = saved.getId();
        repository.save(product2);
        repository.save(product3);
    }

    @Test
    @Order(1)
    @DisplayName("Should list all products with pagination")
    void shouldListAllProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pageInfo.page").value(0))
                .andExpect(jsonPath("$.pageInfo.size").value(10));
    }

    @Test
    @Order(2)
    @DisplayName("Should get product by ID")
    void shouldGetProductById() throws Exception {
        mockMvc.perform(get("/api/products/" + testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Laptop Pro"))
                .andExpect(jsonPath("$.data.category").value("Electronics"));
    }

    @Test
    @Order(3)
    @DisplayName("Should return 404 for non-existent product")
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/nonexistent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(4)
    @DisplayName("Should search products by keyword")
    void shouldSearchProductsByKeyword() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "Laptop")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("Should search products by category")
    void shouldSearchProductsByCategory() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("category", "Electronics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(6)
    @DisplayName("Should get products by IDs in batch")
    void shouldGetProductsByIds() throws Exception {
        mockMvc.perform(post("/api/products/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(testProductId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testProductId));
    }

    @Test
    @Order(7)
    @DisplayName("Should sort products by price ascending")
    void shouldSortProductsByPriceAscending() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "price")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}

