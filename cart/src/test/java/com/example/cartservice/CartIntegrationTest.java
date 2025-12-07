package com.example.cartservice;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "product.service.url=http://localhost:${wiremock.server.port}")
class CartIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private CartRepository cartRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                cartRepository.deleteAll();
        }

        @Test
        void shouldAddToCart() throws Exception {
                Long userId = 1L;
                AddToCartRequest request = new AddToCartRequest("p1", 1);

                stubFor(get(urlEqualTo("/api/products/p1"))
                                .willReturn(aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"p1\",\"name\":\"Product 1\",\"description\":\"Desc\",\"price\":10.0}")));

                mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userId").value(userId))
                                .andExpect(jsonPath("$.items", hasSize(1)))
                                .andExpect(jsonPath("$.items[0].productId").value("p1"));
        }

        @Test
        void shouldGetCart() throws Exception {
                Long userId = 2L;
                // Add item first
                AddToCartRequest request = new AddToCartRequest("p1", 2);

                stubFor(get(urlEqualTo("/api/products/p1"))
                                .willReturn(aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"p1\",\"name\":\"Product 1\",\"description\":\"Desc\",\"price\":10.0}")));

                mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                // Get cart
                mockMvc.perform(MockMvcRequestBuilders.get("/api/cart")
                                .header("X-User-Id", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userId").value(userId))
                                .andExpect(jsonPath("$.items", hasSize(1)));
        }

        @Test
        void shouldRemoveFromCart() throws Exception {
                Long userId = 3L;
                // Add item first
                AddToCartRequest request = new AddToCartRequest("p1", 1);

                stubFor(get(urlEqualTo("/api/products/p1"))
                                .willReturn(aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"p1\",\"name\":\"Product 1\",\"description\":\"Desc\",\"price\":10.0}")));

                mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                // Remove item
                mockMvc.perform(MockMvcRequestBuilders.delete("/api/cart/p1")
                                .header("X-User-Id", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.items", hasSize(0)));
        }
}
