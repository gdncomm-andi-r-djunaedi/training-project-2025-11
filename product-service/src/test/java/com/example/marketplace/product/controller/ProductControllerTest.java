package com.example.marketplace.product.controller;

import com.example.marketplace.product.domain.Product;
import com.example.marketplace.product.dto.ProductRequestDTO;
import com.example.marketplace.product.repo.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository repo;

    @Autowired
    private ObjectMapper mapper;

    // ---------- CREATE PRODUCT ----------
    @Test
    void testCreateProduct() throws Exception {
        ProductRequestDTO req = new ProductRequestDTO();
        req.setName("Laptop");
        req.setDescription("Good laptop");
        req.setPrice(50000.0);

        Product saved = new Product("001", "Laptop", "Good laptop", 50000.0);

        Mockito.when(repo.save(any(Product.class))).thenReturn(saved);

        mockMvc.perform(post("/internal/products")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Laptop"))
                .andExpect(jsonPath("$.data.price").value(50000.0));
    }

    // ---------- GET PRODUCT LIST ----------
    @Test
    void testListProducts() throws Exception {
        Product p1 = new Product("002","Phone", "Android phone", 10000.0);
        p1.setId("p1");

        Page<Product> page = new PageImpl<>(List.of(p1));

        Mockito.when(repo.findByNameContainingIgnoreCase(eq(""), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/internal/products")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value("p1"));
    }

    // ---------- GET PRODUCT BY ID ----------
    @Test
    void testGetProductById() throws Exception {
        Product p = new Product("003","Watch", "Smart Watch", 2999.0);
        p.setId("p123");

        Mockito.when(repo.findById("p123")).thenReturn(Optional.of(p));

        mockMvc.perform(get("/internal/products/p123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("p123"))
                .andExpect(jsonPath("$.data.name").value("Watch"));
    }

    // ---------- PRODUCT NOT FOUND ----------
//    @Test
//    void testProductNotFound() throws Exception {
//        Mockito.when(repo.findById("abc")).thenReturn(Optional.empty());
//
//        mockMvc.perform(get("/internal/products/abc"))
//                .andExpect(status().isNotFound());
//    }
}
