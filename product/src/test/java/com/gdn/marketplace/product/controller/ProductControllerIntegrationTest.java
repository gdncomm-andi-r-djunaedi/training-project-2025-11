package com.gdn.marketplace.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void createProduct_ShouldReturnProduct() throws Exception {
        Product product = new Product();
        product.setName("Integration Product");
        product.setPrice(200.0);

        mockMvc.perform(post("/api/product/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Product"));
    }

    @Test
    void searchProducts_ShouldReturnList() throws Exception {
        Product product = new Product();
        product.setName("Searchable Product");
        product.setPrice(300.0);
        productRepository.save(product);

        mockMvc.perform(get("/api/product/search")
                .param("q", "Searchable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Searchable Product"));
    }
}
