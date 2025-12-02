package com.gdn.training.product.controller;

import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void searchProductsEndpointReturnsPersistedData() throws Exception {
        productRepository.save(Product.builder()
                .name("Gadget Alpha")
                .description("flagship gadget")
                .price(BigDecimal.valueOf(199))
                .quantity(Integer.MAX_VALUE)
                .imageUrl("https://example.com/gadget-alpha")
                .build());
        productRepository.save(Product.builder()
                .name("Widget Beta")
                .description("helper widget")
                .price(BigDecimal.valueOf(49))
                .quantity(Integer.MAX_VALUE)
                .imageUrl("https://example.com/widget-beta")
                .build());

        mockMvc.perform(get("/products").param("query", "Gadget"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Gadget Alpha")));
    }

    @Test
    void getProductEndpointReturnsProduct() throws Exception {
        Product saved = productRepository.save(Product.builder()
                .name("Camera Pro")
                .description("High-end camera")
                .price(BigDecimal.valueOf(899))
                .quantity(Integer.MAX_VALUE)
                .imageUrl("https://example.com/camera-pro")
                .build());

        mockMvc.perform(get("/products/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Camera Pro")))
                .andExpect(jsonPath("$.data.id", is(saved.getId().toString())));
    }
}

