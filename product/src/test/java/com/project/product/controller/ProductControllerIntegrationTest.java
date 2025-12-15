package com.project.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.product.dto.request.CreateProductRequest;
import com.project.product.dto.request.UpdateProductRequest;
import com.project.product.dto.response.PageResponse;
import com.project.product.dto.response.ProductResponse;
import com.project.product.entity.Product;
import com.project.product.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Product API endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private static final String BASE_URL = "/v1/products";
    private static final String TEST_SKU = "TEST-SKU-001";
    private static final String TEST_NAME = "Test Product";
    private static final String TEST_CATEGORY = "Electronics";

    @BeforeEach
    void setUp() {
        // Clean up test data before each test
        productRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should create a new product successfully")
    void testCreateProductSuccess() throws Exception {
        // Given
        CreateProductRequest request = createTestProductRequest();

        // When & Then
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.sku").value(TEST_SKU))
                .andExpect(jsonPath("$.name").value(TEST_NAME))
                .andExpect(jsonPath("$.category").value(TEST_CATEGORY))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        // Verify product was saved in database
        Product savedProduct = productRepository.findBySku(TEST_SKU).orElse(null);
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo(TEST_NAME);
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to create product with duplicate SKU")
    void testCreateProductDuplicateSku() throws Exception {
        // Given - Create a product first
        CreateProductRequest firstRequest = createTestProductRequest();
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - Try to create another product with same SKU
        CreateProductRequest duplicateRequest = createTestProductRequest();
        duplicateRequest.setName("Different Product");

        // Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("Should get product by ID successfully")
    void testGetProductByIdSuccess() throws Exception {
        // Given - Create a product first
        CreateProductRequest request = createTestProductRequest();
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        ProductResponse createdProduct = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProductResponse.class
        );

        // When & Then
        mockMvc.perform(get(BASE_URL + "/" + createdProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProduct.getId()))
                .andExpect(jsonPath("$.sku").value(TEST_SKU))
                .andExpect(jsonPath("$.name").value(TEST_NAME));
    }

    @Test
    @Order(4)
    @DisplayName("Should return 404 when product not found by ID")
    void testGetProductByIdNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_URL + "/nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("Should get product by SKU successfully")
    void testGetProductBySkuSuccess() throws Exception {
        // Given
        CreateProductRequest request = createTestProductRequest();
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/sku/" + TEST_SKU))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value(TEST_SKU))
                .andExpect(jsonPath("$.name").value(TEST_NAME));
    }

    @Test
    @Order(6)
    @DisplayName("Should update product successfully")
    void testUpdateProductSuccess() throws Exception {
        // Given - Create a product first
        CreateProductRequest createRequest = createTestProductRequest();
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        ProductResponse createdProduct = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProductResponse.class
        );

        // When - Update the product
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Product Name");
        updateRequest.setPrice(BigDecimal.valueOf(149.99));
        updateRequest.setDescription("Updated description");

        // Then
        mockMvc.perform(put(BASE_URL + "/" + createdProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product Name"))
                .andExpect(jsonPath("$.price").value(149.99));
    }

    @Test
    @Order(7)
    @DisplayName("Should delete product successfully (soft delete)")
    void testDeleteProductSuccess() throws Exception {
        // Given - Create a product first
        CreateProductRequest request = createTestProductRequest();
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        ProductResponse createdProduct = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProductResponse.class
        );

        // When - Delete the product
        mockMvc.perform(delete(BASE_URL + "/" + createdProduct.getId()))
                .andExpect(status().isNoContent());

        // Then - Verify product is soft deleted (isActive = false)
        Product deletedProduct = productRepository.findById(createdProduct.getId()).orElse(null);
        assertThat(deletedProduct).isNotNull();
        assertThat(deletedProduct.getIsActive()).isFalse();
    }

    @Test
    @Order(8)
    @DisplayName("Should get all products with pagination")
    void testGetAllProductsPagination() throws Exception {
        // Given - Create multiple products
        for (int i = 1; i <= 5; i++) {
            CreateProductRequest request = createTestProductRequest();
            request.setSku("SKU-" + i);
            request.setName("Product " + i);
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // When & Then
        MvcResult result = mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andReturn();
    }

    @Test
    @Order(9)
    @DisplayName("Should get only active products")
    void testGetActiveProductsOnly() throws Exception {
        // Given - Create active and inactive products
        CreateProductRequest activeRequest = createTestProductRequest();
        activeRequest.setSku("ACTIVE-SKU");
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activeRequest)));

        CreateProductRequest inactiveRequest = createTestProductRequest();
        inactiveRequest.setSku("INACTIVE-SKU");
        inactiveRequest.setIsActive(false);
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inactiveRequest)));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("ACTIVE-SKU"));
    }

    @Test
    @Order(10)
    @DisplayName("Should search products by keyword")
    void testSearchProducts() throws Exception {
        // Given - Create products with different names
        CreateProductRequest request1 = createTestProductRequest();
        request1.setSku("SKU-1");
        request1.setName("Laptop Computer");
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        CreateProductRequest request2 = createTestProductRequest();
        request2.setSku("SKU-2");
        request2.setName("Desktop Computer");
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        CreateProductRequest request3 = createTestProductRequest();
        request3.setSku("SKU-3");
        request3.setName("Mobile Phone");
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)));

        // When & Then - Search for "Computer"
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("keyword", "Computer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @Order(11)
    @DisplayName("Should get products by category")
    void testGetProductsByCategory() throws Exception {
        // Given - Create products in different categories
        CreateProductRequest electronicsRequest = createTestProductRequest();
        electronicsRequest.setSku("ELEC-001");
        electronicsRequest.setCategory("Electronics");
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(electronicsRequest)));

        CreateProductRequest clothingRequest = createTestProductRequest();
        clothingRequest.setSku("CLOTH-001");
        clothingRequest.setCategory("Clothing");
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clothingRequest)));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].category").value("Electronics"));
    }

    @Test
    @Order(12)
    @DisplayName("Should get products by IDs (batch)")
    void testGetProductsByIds() throws Exception {
        // Given - Create multiple products
        CreateProductRequest request1 = createTestProductRequest();
        request1.setSku("SKU-1");
        MvcResult result1 = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andReturn();
        ProductResponse product1 = objectMapper.readValue(
                result1.getResponse().getContentAsString(),
                ProductResponse.class
        );

        CreateProductRequest request2 = createTestProductRequest();
        request2.setSku("SKU-2");
        MvcResult result2 = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andReturn();
        ProductResponse product2 = objectMapper.readValue(
                result2.getResponse().getContentAsString(),
                ProductResponse.class
        );

        // When & Then
        List<String> ids = Arrays.asList(product1.getId(), product2.getId());
        mockMvc.perform(post(BASE_URL + "/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // Helper method to create test product request
    private CreateProductRequest createTestProductRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setSku(TEST_SKU);
        request.setName(TEST_NAME);
        request.setDescription("Test product description");
        request.setCategory(TEST_CATEGORY);
        request.setPrice(BigDecimal.valueOf(99.99));
        request.setStock(100);
        request.setIsActive(true);
        request.setTags(Arrays.asList("test", "electronics"));
        return request;
    }
}
