package com.gdn.training.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.product.dto.ProductListRequest;
import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ViewProductListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        Product p1 = new Product("SKU-000001", "Product 1", new BigDecimal("10000"), "Desc 1");
        Product p2 = new Product("SKU-000002", "Product 2", new BigDecimal("20000"), "Desc 2");
        productRepository.save(p1);
        productRepository.save(p2);
    }

    @Test
    void viewProductList_HappyFlow() throws Exception {
        ProductListRequest request = new ProductListRequest();
        request.setPage(0);
        request.setSize(10);

        mockMvc.perform(post("/api/products/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void viewProductList_ProductIdNotFound() throws Exception {
        ProductListRequest request = new ProductListRequest();
        request.setProductId("invalid-id");
        request.setPage(0);
        request.setSize(10);

        mockMvc.perform(post("/api/products/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
