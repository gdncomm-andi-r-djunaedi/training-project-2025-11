package com.marketplace.cart.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.UpdateCartItemRequest;
import com.marketplace.cart.repository.CartRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    private static final String TEST_MEMBER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_PRODUCT_ID = "prod-001";

    @BeforeEach
    void setUp() {
        // Clean up before each test
    }

    @Test
    @Order(1)
    @DisplayName("Should return empty cart for new member")
    void shouldReturnEmptyCartForNewMember() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("X-Member-Id", TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(TEST_MEMBER_ID))
                .andExpect(jsonPath("$.data.totalItems").value(0));
    }

    @Test
    @Order(2)
    @DisplayName("Should add item to cart")
    void shouldAddItemToCart() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(TEST_PRODUCT_ID)
                .productName("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("X-Member-Id", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(2));
    }

    @Test
    @Order(3)
    @DisplayName("Should get cart with items")
    void shouldGetCartWithItems() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("X-Member-Id", TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(TEST_MEMBER_ID))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("Should update cart item quantity")
    void shouldUpdateCartItemQuantity() throws Exception {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        mockMvc.perform(put("/api/cart/items/" + TEST_PRODUCT_ID)
                        .header("X-Member-Id", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(5));
    }

    @Test
    @Order(5)
    @DisplayName("Should add another item to cart")
    void shouldAddAnotherItemToCart() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId("prod-002")
                .productName("Another Product")
                .price(BigDecimal.valueOf(49.99))
                .quantity(1)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("X-Member-Id", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(6)); // 5 + 1
    }

    @Test
    @Order(6)
    @DisplayName("Should remove item from cart")
    void shouldRemoveItemFromCart() throws Exception {
        mockMvc.perform(delete("/api/cart/items/" + TEST_PRODUCT_ID)
                        .header("X-Member-Id", TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(1)); // Only prod-002 left
    }

    @Test
    @Order(7)
    @DisplayName("Should fail without member ID header")
    void shouldFailWithoutMemberIdHeader() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("Should clear entire cart")
    void shouldClearEntireCart() throws Exception {
        mockMvc.perform(delete("/api/cart")
                        .header("X-Member-Id", TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart cleared"));
    }
}

