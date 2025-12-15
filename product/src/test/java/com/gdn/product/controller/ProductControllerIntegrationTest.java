package com.gdn.product.controller;

import com.gdn.product.entity.Product;
import com.gdn.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProductRepository productRepository;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
  }

  private Product createProduct(String name, BigDecimal price, Integer stock) {
    return Product.builder()
        .name(name)
        .description("Description for " + name)
        .price(price)
        .stock(stock)
        .build();
  }

  @Nested
  @DisplayName("GET /products")
  class GetAllProductsTests {

    @Test
    @DisplayName("Should return empty page when no products exist")
    void shouldReturnEmptyPageWhenNoProducts() throws Exception {
      // When
      ResultActions result = mockMvc.perform(get("/products")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content", hasSize(0)))
          .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should return all products with pagination")
    void shouldReturnAllProductsWithPagination() throws Exception {
      // Given
      productRepository.saveAll(List.of(
          createProduct("Laptop", new BigDecimal("1500.00"), 10),
          createProduct("Mouse", new BigDecimal("25.00"), 100),
          createProduct("Keyboard", new BigDecimal("75.00"), 50)
      ));

      // When
      ResultActions result = mockMvc.perform(get("/products")
          .param("page", "0")
          .param("size", "10")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(3)))
          .andExpect(jsonPath("$.totalElements").value(3))
          .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Should return products filtered by name")
    void shouldReturnProductsFilteredByName() throws Exception {
      // Given
      productRepository.saveAll(List.of(
          createProduct("Gaming Laptop", new BigDecimal("2000.00"), 5),
          createProduct("Business Laptop", new BigDecimal("1200.00"), 10),
          createProduct("Mouse", new BigDecimal("25.00"), 100)
      ));

      // When
      ResultActions result = mockMvc.perform(get("/products")
          .param("name", "laptop")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)))
          .andExpect(jsonPath("$.content[*].name", everyItem(containsStringIgnoringCase("laptop"))));
    }

    @Test
    @DisplayName("Should return correct page when paginating")
    void shouldReturnCorrectPageWhenPaginating() throws Exception {
      // Given - Create 15 products
      for (int i = 1; i <= 15; i++) {
        productRepository.save(createProduct("Product " + i, new BigDecimal(i * 10), i));
      }

      // When - Get second page with size 5
      ResultActions result = mockMvc.perform(get("/products")
          .param("page", "1")
          .param("size", "5")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(5)))
          .andExpect(jsonPath("$.totalElements").value(15))
          .andExpect(jsonPath("$.totalPages").value(3))
          .andExpect(jsonPath("$.number").value(1)); // Page index
    }

    @Test
    @DisplayName("Should return empty when search has no matches")
    void shouldReturnEmptyWhenSearchHasNoMatches() throws Exception {
      // Given
      productRepository.saveAll(List.of(
          createProduct("Laptop", new BigDecimal("1500.00"), 10),
          createProduct("Mouse", new BigDecimal("25.00"), 100)
      ));

      // When
      ResultActions result = mockMvc.perform(get("/products")
          .param("name", "nonexistent")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(0)))
          .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should use default pagination values")
    void shouldUseDefaultPaginationValues() throws Exception {
      // Given
      for (int i = 1; i <= 15; i++) {
        productRepository.save(createProduct("Product " + i, new BigDecimal(i * 10), i));
      }

      // When - No pagination params (defaults: page=0, size=10)
      ResultActions result = mockMvc.perform(get("/products")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(10))) // Default size is 10
          .andExpect(jsonPath("$.number").value(0)); // Default page is 0
    }
  }

  @Nested
  @DisplayName("GET /products/{id}")
  class GetProductByIdTests {

    @Test
    @DisplayName("Should return product when found")
    void shouldReturnProductWhenFound() throws Exception {
      // Given
      Product savedProduct = productRepository.save(
          createProduct("Test Product", new BigDecimal("99.99"), 25)
      );

      // When
      ResultActions result = mockMvc.perform(get("/products/{id}", savedProduct.getId())
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(savedProduct.getId()))
          .andExpect(jsonPath("$.name").value("Test Product"))
          .andExpect(jsonPath("$.price").value(99.99))
          .andExpect(jsonPath("$.stock").value(25));
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void shouldReturn404WhenProductNotFound() throws Exception {
      // When
      ResultActions result = mockMvc.perform(get("/products/{id}", "nonexistent-id")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return correct product among multiple")
    void shouldReturnCorrectProductAmongMultiple() throws Exception {
      // Given
      Product product1 = productRepository.save(createProduct("Product 1", new BigDecimal("100"), 10));
      Product product2 = productRepository.save(createProduct("Product 2", new BigDecimal("200"), 20));
      Product product3 = productRepository.save(createProduct("Product 3", new BigDecimal("300"), 30));

      // When
      ResultActions result = mockMvc.perform(get("/products/{id}", product2.getId())
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(product2.getId()))
          .andExpect(jsonPath("$.name").value("Product 2"))
          .andExpect(jsonPath("$.price").value(200))
          .andExpect(jsonPath("$.stock").value(20));
    }
  }

  @Nested
  @DisplayName("Search Functionality")
  class SearchTests {

    @BeforeEach
    void setUpProducts() {
      productRepository.saveAll(List.of(
          createProduct("Apple MacBook Pro", new BigDecimal("2499.00"), 5),
          createProduct("Apple iPhone 15", new BigDecimal("999.00"), 20),
          createProduct("Samsung Galaxy S24", new BigDecimal("899.00"), 15),
          createProduct("Dell XPS 15", new BigDecimal("1799.00"), 8),
          createProduct("Apple Watch Series 9", new BigDecimal("399.00"), 30)
      ));
    }

    @Test
    @DisplayName("Should search case-insensitively")
    void shouldSearchCaseInsensitively() throws Exception {
      // When - Search with uppercase
      ResultActions result = mockMvc.perform(get("/products")
          .param("name", "APPLE")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(3)))
          .andExpect(jsonPath("$.content[*].name", everyItem(containsStringIgnoringCase("apple"))));
    }

    @Test
    @DisplayName("Should search with partial match")
    void shouldSearchWithPartialMatch() throws Exception {
      // When
      ResultActions result = mockMvc.perform(get("/products")
          .param("name", "Pro")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].name").value("Apple MacBook Pro"));
    }

    @Test
    @DisplayName("Should combine search with pagination")
    void shouldCombineSearchWithPagination() throws Exception {
      // When - Search "apple" with page size 2
      ResultActions result = mockMvc.perform(get("/products")
          .param("name", "apple")
          .param("page", "0")
          .param("size", "2")
          .contentType(MediaType.APPLICATION_JSON));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)))
          .andExpect(jsonPath("$.totalElements").value(3))
          .andExpect(jsonPath("$.totalPages").value(2));
    }
  }
}
