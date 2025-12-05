package com.blibli.productModule.controller;

import com.blibli.productModule.dto.ProductDto;
import com.blibli.productModule.dto.ProductSearchResponseDto;
import com.blibli.productModule.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProductService productService;

  @Autowired
  private ObjectMapper objectMapper;

  private ProductDto productDto;
  private ProductSearchResponseDto productSearchResponseDto;

  @BeforeEach
  void setUp() {
    productDto = new ProductDto();
    productDto.setProductId("PROD123");
    productDto.setName("Test Product");
    productDto.setDescription("Test Description");
    productDto.setCategory("Electronics");
    productDto.setPrice(new BigDecimal("100.00"));
    productDto.setBrand("Test Brand");
    productDto.setImageUrl("http://gmail.com/image.jpg");
    productDto.setIsActive(true);

    List<ProductDto> content = new ArrayList<>();
    content.add(productDto);

    productSearchResponseDto = new ProductSearchResponseDto();
    productSearchResponseDto.setContent(content);
    productSearchResponseDto.setPage(0);
    productSearchResponseDto.setSize(10);
    productSearchResponseDto.setTotalElements(1);
    productSearchResponseDto.setTotalPages(1);
  }

  @Test
  void testCreateProduct_Success() throws Exception {
    when(productService.createProduct(any(ProductDto.class))).thenReturn(productDto);

    mockMvc.perform(post("/api/products/create").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(productDto))).andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.value.productId").value("PROD123"))
        .andExpect(jsonPath("$.value.name").value("Test Product"));
  }

  @Test
  void testSearchProducts_Success() throws Exception {
    when(productService.searchProducts(any(), any(), anyInt(), anyInt())).thenReturn(
        productSearchResponseDto);

    mockMvc.perform(get("/api/products/search").param("searchTerm", "laptop").param("page", "0")
            .param("size", "10")).andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.value.content").isArray())
        .andExpect(jsonPath("$.value.totalElements").value(1));
  }

  @Test
  void testSearchProducts_WithCategory() throws Exception {
    when(productService.searchProducts(any(), any(), anyInt(), anyInt())).thenReturn(
        productSearchResponseDto);

    mockMvc.perform(
            get("/api/products/search").param("searchTerm", "laptop").param("category",
                    "Electronics")
                .param("page", "0").param("size", "10")).andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void testGetAllProducts_Success() throws Exception {
    when(productService.getAllProducts(any(), anyInt(), anyInt())).thenReturn(
        productSearchResponseDto);

    mockMvc.perform(get("/api/products/listing").param("page", "0").param("size", "10"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.value.content").isArray());
  }

  @Test
  void testGetAllProducts_WithCategory() throws Exception {
    when(productService.getAllProducts(any(), anyInt(), anyInt())).thenReturn(
        productSearchResponseDto);

    mockMvc.perform(get("/api/products/listing").param("category", "Electronics").param("page", "0")
            .param("size", "10")).andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void testGetProductById_Success() throws Exception {
    when(productService.getProductById("PROD123")).thenReturn(productDto);

    mockMvc.perform(get("/api/products/detail/PROD123")).andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.value.productId").value("PROD123"))
        .andExpect(jsonPath("$.value.name").value("Test Product"));
  }

  @Test
  void testDeleteProduct_Success() throws Exception {
    doNothing().when(productService).deleteProduct("PROD123");

    mockMvc.perform(delete("/api/products/delete/PROD123")).andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.value").value("Product deleted successfully"));
  }
}

