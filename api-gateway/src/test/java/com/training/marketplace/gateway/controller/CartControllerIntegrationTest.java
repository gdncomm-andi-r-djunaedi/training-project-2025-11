package com.training.marketplace.gateway.controller;

import com.training.marketplace.cart.modal.response.DefaultCartResponse;
import com.training.marketplace.gateway.dto.cart.AddProductToCartRequestDTO;
import com.training.marketplace.gateway.service.CartClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartClientService cartClientService;

    @Test
    void testPostAddProductToCart_Success() throws Exception {
        AddProductToCartRequestDTO request = new AddProductToCartRequestDTO();
        request.setUserId("user1");
        request.setProductId("product1");
        request.setQuantity(2);

        DefaultCartResponse response = DefaultCartResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Product added to cart successfully")
                .build();

        when(cartClientService.addProductToCart(any())).thenReturn(response);

        mockMvc.perform(post("/api/cart/addToCart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product added to cart successfully"));
    }
}
