package com.gdn.training.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.product.dto.SearchProductRequest;
import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SearchProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        Product p1 = new Product("SKU-000001", "Apple iPhone", 10000000.0, "Smartphone");
        Product p2 = new Product("SKU-000002", "Samsung Galaxy", 9000000.0, "Smartphone");
        Product p3 = new Product("SKU-000003", "Apple iPad", 8000000.0, "Tablet");
        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
    }

    @Test
    void searchProduct_HappyFlow() throws Exception {
        SearchProductRequest request = new SearchProductRequest();
        request.setProductName("Apple");
        request.setPage(0);
        request.setSize(10);

        mockMvc.perform(post("/api/products/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }
}
