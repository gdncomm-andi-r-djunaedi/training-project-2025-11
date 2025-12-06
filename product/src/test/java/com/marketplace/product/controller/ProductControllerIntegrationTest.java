package com.marketplace.product.controller;

import com.marketplace.product.document.Product;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ProductRepository productRepository;

        @BeforeEach
        void setUp() {
                productRepository.deleteAll();
        }

        @Test
        void getProductById_ExistingProduct_ReturnsProduct() throws Exception {
                Product product = productRepository.save(Product.builder()
                                .name("Test Product")
                                .description("Test Description")
                                .price(new BigDecimal("99.99"))
                                .category("Electronics")
                                .stock(100)
                                .build());

                mockMvc.perform(get("/api/product/{id}", product.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(product.getId()))
                                .andExpect(jsonPath("$.data.name").value("Test Product"))
                                .andExpect(jsonPath("$.data.description").value("Test Description"))
                                .andExpect(jsonPath("$.data.price").value(99.99))
                                .andExpect(jsonPath("$.data.category").value("Electronics"));
        }

        @Test
        void getProductById_NonExistingProduct_ReturnsNotFound() throws Exception {
                mockMvc.perform(get("/api/product/{id}", "non-existent-id")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Product not found with ID: non-existent-id"));
        }

        @Test
        void searchProducts_NoParams_ReturnsAllProducts() throws Exception {
                productRepository.save(Product.builder().name("Product 1").price(new BigDecimal("10")).build());
                productRepository.save(Product.builder().name("Product 2").price(new BigDecimal("20")).build());
                productRepository.save(Product.builder().name("Product 3").price(new BigDecimal("30")).build());

                mockMvc.perform(get("/api/product/search")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.content", hasSize(3)))
                                .andExpect(jsonPath("$.data.totalElements").value(3));
        }

        @Test
        void searchProducts_WithName_ReturnsMatchingProducts() throws Exception {
                productRepository.save(Product.builder().name("Gaming Laptop").price(new BigDecimal("999")).build());
                productRepository.save(Product.builder().name("Business Laptop").price(new BigDecimal("799")).build());
                productRepository.save(Product.builder().name("Desktop Computer").price(new BigDecimal("599")).build());

                mockMvc.perform(get("/api/product/search")
                                .param("name", "laptop")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content", hasSize(2)))
                                .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        void searchProducts_CaseInsensitive_ReturnsMatches() throws Exception {
                productRepository.save(Product.builder().name("iPhone Pro").price(new BigDecimal("999")).build());
                productRepository.save(Product.builder().name("IPHONE Mini").price(new BigDecimal("799")).build());

                mockMvc.perform(get("/api/product/search")
                                .param("name", "IPHONE")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(2)));
        }

        @Test
        void searchProducts_NoMatches_ReturnsEmptyPage() throws Exception {
                productRepository.save(Product.builder().name("Mouse").price(new BigDecimal("29")).build());

                mockMvc.perform(get("/api/product/search")
                                .param("name", "keyboard")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content", hasSize(0)))
                                .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void searchProducts_WithPagination_ReturnsPagedResults() throws Exception {
                for (int i = 1; i <= 15; i++) {
                        productRepository.save(Product.builder()
                                        .name("Product " + i)
                                        .price(new BigDecimal(i * 10))
                                        .build());
                }

                // First page
                mockMvc.perform(get("/api/product/search")
                                .param("page", "0")
                                .param("size", "5")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(5)))
                                .andExpect(jsonPath("$.data.totalElements").value(15))
                                .andExpect(jsonPath("$.data.totalPages").value(3))
                                .andExpect(jsonPath("$.data.number").value(0));

                // Second page
                mockMvc.perform(get("/api/product/search")
                                .param("page", "1")
                                .param("size", "5")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(5)))
                                .andExpect(jsonPath("$.data.number").value(1));
        }

        @Test
        void searchProducts_DefaultPagination_Uses10ItemsPerPage() throws Exception {
                for (int i = 1; i <= 15; i++) {
                        productRepository.save(Product.builder()
                                        .name("Item " + i)
                                        .price(new BigDecimal(i))
                                        .build());
                }

                mockMvc.perform(get("/api/product/search")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(10)))
                                .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        void searchProducts_WildcardSearch_PartialMatch() throws Exception {
                productRepository.save(Product.builder().name("Smartphone Case").price(new BigDecimal("19")).build());
                productRepository.save(Product.builder().name("Phone Charger").price(new BigDecimal("29")).build());
                productRepository.save(Product.builder().name("Microphone").price(new BigDecimal("99")).build());

                mockMvc.perform(get("/api/product/search")
                                .param("name", "phone")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(3)));
        }

        @Test
        void searchProducts_EmptyName_ReturnsAllProducts() throws Exception {
                productRepository.save(Product.builder().name("Product A").price(new BigDecimal("10")).build());
                productRepository.save(Product.builder().name("Product B").price(new BigDecimal("20")).build());

                mockMvc.perform(get("/api/product/search")
                                .param("name", "")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(2)));
        }

        @Test
        void getProductById_ProductWithAllFields_ReturnsCompleteProduct() throws Exception {
                Product product = productRepository.save(Product.builder()
                                .name("Complete Product")
                                .description("Full description here")
                                .price(new BigDecimal("149.99"))
                                .category("Category A")
                                .stock(50)
                                .build());

                mockMvc.perform(get("/api/product/{id}", product.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.name").value("Complete Product"))
                                .andExpect(jsonPath("$.data.description").value("Full description here"))
                                .andExpect(jsonPath("$.data.price").value(149.99))
                                .andExpect(jsonPath("$.data.category").value("Category A"));
        }
}
