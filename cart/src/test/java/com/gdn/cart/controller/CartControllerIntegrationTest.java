package com.gdn.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.cart.client.ProductClient;
import com.gdn.cart.client.model.ProductResponse;
import com.gdn.cart.entity.Cart;
import com.gdn.cart.entity.CartItem;
import com.gdn.cart.exception.DataNotFoundException;
import com.gdn.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerIntegrationTest {

  private static final String USER_ID_HEADER = "X-User-Id";
  private static final String TEST_USER_ID = "member-123";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ProductClient productClient;

  @BeforeEach
  void setUp() {
    cartRepository.deleteAll();
    setupProductClientMocks();
  }

  private void setupProductClientMocks() {
    // Mock product responses
    when(productClient.getProductById("product-1"))
        .thenReturn(ProductResponse.builder()
            .id("product-1")
            .name("Laptop")
            .price(new BigDecimal("1500.00"))
            .stock(10)
            .build());

    when(productClient.getProductById("product-2"))
        .thenReturn(ProductResponse.builder()
            .id("product-2")
            .name("Mouse")
            .price(new BigDecimal("25.00"))
            .stock(100)
            .build());

    when(productClient.getProductById("product-3"))
        .thenReturn(ProductResponse.builder()
            .id("product-3")
            .name("Keyboard")
            .price(new BigDecimal("75.00"))
            .stock(50)
            .build());

    // Mock not found product
    when(productClient.getProductById("invalid-product"))
        .thenThrow(new DataNotFoundException());
  }

  private Cart createCartWithItems(String memberId, List<CartItem> items) {
    return cartRepository.save(Cart.builder()
        .memberId(memberId)
        .items(items)
        .build());
  }

  @Nested
  @DisplayName("GET /carts")
  class GetCartTests {

    @Test
    @DisplayName("Should return empty cart for new user")
    void shouldReturnEmptyCartForNewUser() throws Exception {
      mockMvc.perform(get("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.memberId").value(TEST_USER_ID))
          .andExpect(jsonPath("$.items").isArray())
          .andExpect(jsonPath("$.items", hasSize(0)))
          .andExpect(jsonPath("$.totalItems").value(0))
          .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    @DisplayName("Should return cart with items")
    void shouldReturnCartWithItems() throws Exception {
      // Given - Create cart with items
      List<CartItem> items = new ArrayList<>();
      items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(2)
          .build());
      items.add(CartItem.builder()
          .productId("product-2")
          .productName("Mouse")
          .price(new BigDecimal("25.00"))
          .quantity(1)
          .build());
      createCartWithItems(TEST_USER_ID, items);

      // When & Then
      mockMvc.perform(get("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.memberId").value(TEST_USER_ID))
          .andExpect(jsonPath("$.items", hasSize(2)))
          .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
          .andExpect(jsonPath("$.items[0].quantity").value(2))
          .andExpect(jsonPath("$.items[0].subtotal").value(3000.00))
          .andExpect(jsonPath("$.items[1].productName").value("Mouse"))
          .andExpect(jsonPath("$.totalItems").value(3))
          .andExpect(jsonPath("$.totalPrice").value(3025.00));
    }

    @Test
    @DisplayName("Should return different carts for different users")
    void shouldReturnDifferentCartsForDifferentUsers() throws Exception {
      // Given - Create carts for two users
      List<CartItem> user1Items = new ArrayList<>();
      user1Items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(1)
          .build());
      createCartWithItems("user-1", user1Items);

      List<CartItem> user2Items = new ArrayList<>();
      user2Items.add(CartItem.builder()
          .productId("product-2")
          .productName("Mouse")
          .price(new BigDecimal("25.00"))
          .quantity(3)
          .build());
      createCartWithItems("user-2", user2Items);

      // When & Then - User 1
      mockMvc.perform(get("/carts")
              .header(USER_ID_HEADER, "user-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
          .andExpect(jsonPath("$.totalItems").value(1));

      // When & Then - User 2
      mockMvc.perform(get("/carts")
              .header(USER_ID_HEADER, "user-2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items[0].productName").value("Mouse"))
          .andExpect(jsonPath("$.totalItems").value(3));
    }

    @Test
    @DisplayName("Should return 400 when X-User-Id header is missing")
    void shouldReturn400WhenUserIdMissing() throws Exception {
      mockMvc.perform(get("/carts"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("POST /carts")
  class AddToCartTests {

    @Test
    @DisplayName("Should add product to empty cart")
    void shouldAddProductToEmptyCart() throws Exception {
      String requestBody = """
          {
            "productId": "product-1",
            "quantity": 2
          }
          """;

      mockMvc.perform(post("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.memberId").value(TEST_USER_ID))
          .andExpect(jsonPath("$.items", hasSize(1)))
          .andExpect(jsonPath("$.items[0].productId").value("product-1"))
          .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
          .andExpect(jsonPath("$.items[0].price").value(1500.00))
          .andExpect(jsonPath("$.items[0].quantity").value(2))
          .andExpect(jsonPath("$.items[0].subtotal").value(3000.00))
          .andExpect(jsonPath("$.totalItems").value(2))
          .andExpect(jsonPath("$.totalPrice").value(3000.00));
    }

    @Test
    @DisplayName("Should add new product to existing cart")
    void shouldAddNewProductToExistingCart() throws Exception {
      // Given - Create cart with one item
      List<CartItem> items = new ArrayList<>();
      items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(1)
          .build());
      createCartWithItems(TEST_USER_ID, items);

      // When - Add different product
      String requestBody = """
          {
            "productId": "product-2",
            "quantity": 3
          }
          """;

      mockMvc.perform(post("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items", hasSize(2)))
          .andExpect(jsonPath("$.totalItems").value(4))
          .andExpect(jsonPath("$.totalPrice").value(1575.00)); // 1500 + 75 (25 * 3)
    }

    @Test
    @DisplayName("Should increase quantity when adding existing product")
    void shouldIncreaseQuantityWhenAddingExistingProduct() throws Exception {
      // Given - Create cart with one item
      List<CartItem> items = new ArrayList<>();
      items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(1)
          .build());
      createCartWithItems(TEST_USER_ID, items);

      // When - Add same product
      String requestBody = """
          {
            "productId": "product-1",
            "quantity": 2
          }
          """;

      mockMvc.perform(post("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items", hasSize(1)))
          .andExpect(jsonPath("$.items[0].quantity").value(3))
          .andExpect(jsonPath("$.totalItems").value(3))
          .andExpect(jsonPath("$.totalPrice").value(4500.00));
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void shouldReturn404WhenProductNotFound() throws Exception {
      String requestBody = """
          {
            "productId": "invalid-product",
            "quantity": 1
          }
          """;

      mockMvc.perform(post("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when productId is missing")
    void shouldReturn400WhenProductIdMissing() throws Exception {
      String requestBody = """
          {
            "quantity": 1
          }
          """;

      mockMvc.perform(post("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when quantity is less than 1")
    void shouldReturn400WhenQuantityLessThan1() throws Exception {
      String requestBody = """
          {
            "productId": "product-1",
            "quantity": 0
          }
          """;

      mockMvc.perform(post("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when quantity is missing")
    void shouldReturn400WhenQuantityMissing() throws Exception {
      String requestBody = """
          {
            "productId": "product-1"
          }
          """;

      mockMvc.perform(post("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("DELETE /carts/items/{productId}")
  class RemoveFromCartTests {

    @Test
    @DisplayName("Should remove product from cart")
    void shouldRemoveProductFromCart() throws Exception {
      // Given - Create cart with two items
      List<CartItem> items = new ArrayList<>();
      items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(1)
          .build());
      items.add(CartItem.builder()
          .productId("product-2")
          .productName("Mouse")
          .price(new BigDecimal("25.00"))
          .quantity(2)
          .build());
      createCartWithItems(TEST_USER_ID, items);

      // When - Remove product-1
      mockMvc.perform(delete("/carts/items/product-1")
              .header(USER_ID_HEADER, TEST_USER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items", hasSize(1)))
          .andExpect(jsonPath("$.items[0].productId").value("product-2"))
          .andExpect(jsonPath("$.totalItems").value(2))
          .andExpect(jsonPath("$.totalPrice").value(50.00));
    }

    @Test
    @DisplayName("Should remove last product from cart")
    void shouldRemoveLastProductFromCart() throws Exception {
      // Given - Create cart with one item
      List<CartItem> items = new ArrayList<>();
      items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(1)
          .build());
      createCartWithItems(TEST_USER_ID, items);

      // When - Remove the only item
      mockMvc.perform(delete("/carts/items/product-1")
              .header(USER_ID_HEADER, TEST_USER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items", hasSize(0)))
          .andExpect(jsonPath("$.totalItems").value(0))
          .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    @DisplayName("Should return 404 when product not in cart")
    void shouldReturn404WhenProductNotInCart() throws Exception {
      // Given - Create cart with one item
      List<CartItem> items = new ArrayList<>();
      items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(1)
          .build());
      createCartWithItems(TEST_USER_ID, items);

      // When - Try to remove non-existent product
      mockMvc.perform(delete("/carts/items/product-99")
              .header(USER_ID_HEADER, TEST_USER_ID))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when cart is empty")
    void shouldReturn404WhenCartIsEmpty() throws Exception {
      mockMvc.perform(delete("/carts/items/product-1")
              .header(USER_ID_HEADER, TEST_USER_ID))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Cart Calculation Tests")
  class CartCalculationTests {

    @Test
    @DisplayName("Should calculate subtotal correctly for each item")
    void shouldCalculateSubtotalCorrectly() throws Exception {
      // Given
      List<CartItem> items = new ArrayList<>();
      items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(3)
          .build());
      createCartWithItems(TEST_USER_ID, items);

      // When & Then
      mockMvc.perform(get("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items[0].subtotal").value(4500.00)); // 1500 * 3
    }

    @Test
    @DisplayName("Should calculate total price correctly for multiple items")
    void shouldCalculateTotalPriceCorrectly() throws Exception {
      // Given
      List<CartItem> items = new ArrayList<>();
      items.add(CartItem.builder()
          .productId("product-1")
          .productName("Laptop")
          .price(new BigDecimal("1500.00"))
          .quantity(2)
          .build());
      items.add(CartItem.builder()
          .productId("product-2")
          .productName("Mouse")
          .price(new BigDecimal("25.00"))
          .quantity(4)
          .build());
      items.add(CartItem.builder()
          .productId("product-3")
          .productName("Keyboard")
          .price(new BigDecimal("75.00"))
          .quantity(1)
          .build());
      createCartWithItems(TEST_USER_ID, items);

      // When & Then
      // Total = (1500*2) + (25*4) + (75*1) = 3000 + 100 + 75 = 3175
      mockMvc.perform(get("/carts")
              .header(USER_ID_HEADER, TEST_USER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalPrice").value(3175.00))
          .andExpect(jsonPath("$.totalItems").value(7)); // 2 + 4 + 1
    }
  }
}

