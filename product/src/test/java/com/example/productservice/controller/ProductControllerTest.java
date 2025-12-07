package com.example.productservice.controller;

import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getProducts_shouldReturnPageOfProducts() throws Exception {
        Product p1 = new Product("1", "P1", "Desc1", BigDecimal.TEN);
        Product p2 = new Product("2", "P2", "Desc2", BigDecimal.TEN);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(p1, p2));

        when(productService.getProducts(anyInt(), anyInt())).thenReturn(productPage);

        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("P1"))
                .andExpect(jsonPath("$.content[0].description").value("Desc1"))
                .andExpect(jsonPath("$.content[0].price").value(10));
    }

    @Test
    void getProduct_shouldReturnProduct_whenFound() throws Exception {
        Product p1 = new Product("1", "P1", "Desc1", BigDecimal.TEN);
        when(productService.getProduct("1")).thenReturn(p1);

        mockMvc.perform(get("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("P1"))
                .andExpect(jsonPath("$.description").value("Desc1"))
                .andExpect(jsonPath("$.price").value(10));
    }

    @Test
    void getProduct_shouldReturn404_whenNotFound() throws Exception {
        when(productService.getProduct("999")).thenThrow(
                new com.example.productservice.exception.ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found with id: 999"));
    }

    @Test
    void searchProducts_shouldReturnMatchingProducts() throws Exception {
        Product p1 = new Product("1", "P1", "Desc1", BigDecimal.TEN);
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(p1));

        when(productService.searchProducts(anyString(), anyInt(), anyInt())).thenReturn(productPage);

        mockMvc.perform(get("/api/products/search")
                .param("name", "P1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("P1"))
                .andExpect(jsonPath("$.content[0].description").value("Desc1"))
                .andExpect(jsonPath("$.content[0].price").value(10));
    }
}
