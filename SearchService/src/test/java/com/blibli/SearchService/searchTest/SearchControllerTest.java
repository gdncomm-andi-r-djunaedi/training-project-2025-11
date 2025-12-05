package com.blibli.SearchService.searchTest;

import com.blibli.SearchService.controller.ProductSearchController;
import com.blibli.SearchService.entity.ProductDocument;
import com.blibli.SearchService.exception.InvalidSearchException;
import com.blibli.SearchService.exception.ProductNotFoundException;
import com.blibli.SearchService.service.ProductSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductSearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductSearchService service;

    @Autowired

    private ObjectMapper objectMapper;

    @Test
    void searchByName_shouldReturnPage() throws Exception {
        ProductDocument doc = ProductDocument.builder()
                .sku("SKU-1")
                .productName("iPhone")
                .build();

        Page<ProductDocument> page =
                new PageImpl<>(List.of(doc), PageRequest.of(0, 10), 1);

        when(service.searchByName("iPhone", 0, 10)).thenReturn(page);

        mockMvc.perform(
                        get("/name")
                                .param("name", "iPhone")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].sku").value("SKU-1"));
    }

    @Test
    void searchByName_shouldReturn400_forEmptyName() throws Exception {
        when(service.searchByName(any(), anyInt(), anyInt()))
                .thenThrow(new InvalidSearchException("Product name must not be empty"));

        mockMvc.perform(
                        get("/name").param("name", "")
                )
                .andExpect(status().isBadRequest());
    }

    // ---------- SEARCH BY SKU ----------

    @Test
    void searchBySku_shouldReturnProduct() throws Exception {
        ProductDocument doc = ProductDocument.builder()
                .sku("SKU-1")
                .productName("iPhone")
                .build();

        when(service.searchBySku("SKU-1")).thenReturn(doc);

        mockMvc.perform(
                        get("/sku")
                                .param("sku", "SKU-1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("SKU-1"));
    }

    @Test
    void searchBySku_shouldReturn404_whenNotFound() throws Exception {
        when(service.searchBySku("SKU-X"))
                .thenThrow(new ProductNotFoundException("No product found"));

        mockMvc.perform(
                        get("/sku")
                                .param("sku", "SKU-X")
                )
                .andExpect(status().isNotFound());
    }
}
