package com.marketplace.product.controller;

import com.marketplace.product.document.Product;
import com.marketplace.product.document.ProductSearchDoc;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.repository.ProductSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

    @MockBean
    private ProductSearchRepository productSearchRepository;

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
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found: non-existent-id"));
    }

    @Test
    void searchProducts_NoParams_ReturnsAllProducts() throws Exception {
        productRepository.save(Product.builder().name("Product 1").price(new BigDecimal("10")).build());
        productRepository.save(Product.builder().name("Product 2").price(new BigDecimal("20")).build());
        productRepository.save(Product.builder().name("Product 3").price(new BigDecimal("30")).build());

        List<ProductSearchDoc> docs = new ArrayList<>();
        docs.add(ProductSearchDoc.builder().name("Product 1").price(new BigDecimal("10")).build());
        docs.add(ProductSearchDoc.builder().name("Product 2").price(new BigDecimal("20")).build());
        docs.add(ProductSearchDoc.builder().name("Product 3").price(new BigDecimal("30")).build());

        when(productSearchRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(docs));

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

        List<ProductSearchDoc> docs = new ArrayList<>();
        docs.add(ProductSearchDoc.builder().name("Gaming Laptop").price(new BigDecimal("999")).build());
        docs.add(ProductSearchDoc.builder().name("Business Laptop").price(new BigDecimal("799")).build());

        when(productSearchRepository.findByNameOrDescriptionContaining(any(), any(), any()))
                .thenReturn(new PageImpl<>(docs));

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

        List<ProductSearchDoc> docs = new ArrayList<>();
        docs.add(ProductSearchDoc.builder().name("iPhone Pro").price(new BigDecimal("999")).build());
        docs.add(ProductSearchDoc.builder().name("IPHONE Mini").price(new BigDecimal("799")).build());

        when(productSearchRepository.findByNameOrDescriptionContaining(any(), any(), any()))
                .thenReturn(new PageImpl<>(docs));

        mockMvc.perform(get("/api/product/search")
                        .param("name", "IPHONE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    void searchProducts_NoMatches_ReturnsEmptyPage() throws Exception {
        productRepository.save(Product.builder().name("Mouse").price(new BigDecimal("29")).build());

        when(productSearchRepository.findByNameOrDescriptionContaining(any(), any(), any()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

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
        List<ProductSearchDoc> allDocs = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            productRepository.save(Product.builder()
                    .name("Product " + i)
                    .price(new BigDecimal(i * 10))
                    .build());
            allDocs.add(ProductSearchDoc.builder()
                    .name("Product " + i)
                    .price(new BigDecimal(i * 10))
                    .build());
        }

        // Mock first page
        List<ProductSearchDoc> page1Docs = allDocs.subList(0, 5);
        when(productSearchRepository.findAll(ArgumentMatchers
                .argThat((Pageable p) -> p != null && p.getPageNumber() == 0)))
                .thenReturn(new PageImpl<>(page1Docs, PageRequest.of(0, 5), 15));

        // Mock second page
        List<ProductSearchDoc> page2Docs = allDocs.subList(5, 10);
        when(productSearchRepository.findAll(ArgumentMatchers
                .argThat((Pageable p) -> p != null && p.getPageNumber() == 1)))
                .thenReturn(new PageImpl<>(page2Docs, PageRequest.of(1, 5), 15));

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
        List<ProductSearchDoc> docs = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            productRepository.save(Product.builder()
                    .name("Item " + i)
                    .price(new BigDecimal(i))
                    .build());
            if (i <= 10) {
                docs.add(ProductSearchDoc.builder()
                        .name("Item " + i)
                        .price(new BigDecimal(i))
                        .build());
            }
        }

        when(productSearchRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(docs, PageRequest.of(0, 10), 15));

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

        List<ProductSearchDoc> docs = new ArrayList<>();
        docs.add(ProductSearchDoc.builder().name("Smartphone Case").price(new BigDecimal("19")).build());
        docs.add(ProductSearchDoc.builder().name("Phone Charger").price(new BigDecimal("29")).build());
        docs.add(ProductSearchDoc.builder().name("Microphone").price(new BigDecimal("99")).build());

        when(productSearchRepository.findByNameOrDescriptionContaining(any(), any(), any()))
                .thenReturn(new PageImpl<>(docs));

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

        List<ProductSearchDoc> docs = new ArrayList<>();
        docs.add(ProductSearchDoc.builder().name("Product A").price(new BigDecimal("10")).build());
        docs.add(ProductSearchDoc.builder().name("Product B").price(new BigDecimal("20")).build());

        when(productSearchRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(docs));

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
