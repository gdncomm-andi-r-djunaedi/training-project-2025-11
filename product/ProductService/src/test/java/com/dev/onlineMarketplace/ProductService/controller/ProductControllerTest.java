package com.dev.onlineMarketplace.ProductService.controller;

import com.dev.onlineMarketplace.ProductService.dto.ProductDTO;
import com.dev.onlineMarketplace.ProductService.dto.ProductSearchResponse;
import com.dev.onlineMarketplace.ProductService.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void searchProducts_ShouldReturnOk() throws Exception {
        ProductSearchResponse response = new ProductSearchResponse(1, 10, 1, Collections.emptyList());
        when(productService.searchProducts(anyString(), anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products/search")
                .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Products found"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void getProductByIdOrSku_ShouldReturnOk() throws Exception {
        ProductDTO productDTO = new ProductDTO("1", "Test", "SKU-TEST", "Desc", 100.0, "Cat", "img");
        when(productService.getProductByIdOrSku("1")).thenReturn(productDTO);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test"))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
