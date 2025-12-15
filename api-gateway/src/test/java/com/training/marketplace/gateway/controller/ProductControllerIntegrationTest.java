package com.training.marketplace.gateway.controller;


import com.training.marketplace.gateway.dto.product.GetProductDetailRequestDTO;
import com.training.marketplace.gateway.service.ProductClientService;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;

import com.training.marketplace.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductClientService productClientService;

    @Test
    void testGetProductDetail_Success() throws Exception {
        GetProductDetailRequestDTO request = new GetProductDetailRequestDTO();
        request.setProductId("product1");

        Product product = Product.newBuilder()
                .setProductId("product1")
                .setProductName("Test Product")
                .setProductPrice(100)
                .setProductDetail("Product Detail")
                .setProductNotes("Product Notes")
                .setProductImage("product_image.jpg")
                .build();

        GetProductDetailResponse response = GetProductDetailResponse.newBuilder()
                .setProduct(product)
                .build();

        when(productClientService.getProductDetail(any())).thenReturn(response);

        mockMvc.perform(get("/api/product/getProductDetail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.productId").value("product1"))
                .andExpect(jsonPath("$.product.productName").value("Test Product"))
                .andExpect(jsonPath("$.product.productPrice").value(100));
    }
}
