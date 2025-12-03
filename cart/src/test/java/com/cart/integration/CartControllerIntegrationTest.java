package com.cart.integration;

import com.cart.dto.request.AddItemRequest;
import com.cart.dto.request.RemoveItemRequest;
import com.cart.dto.request.UpdateItemQuantityRequest;
import com.cart.entity.Cart;
import com.cart.entity.CartItem;
import com.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CartControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private CartService cartService;

    @Test
    void testGetCartByCustomerId() {
        UUID customerId = UUID.randomUUID();

        Cart mockCart = new Cart();
        mockCart.setId(UUID.randomUUID());
        mockCart.setCustomerId(customerId);

        Mockito.when(cartService.getOrCreateCart(customerId))
                .thenReturn(mockCart);

        ResponseEntity<Cart> response =
                restTemplate.getForEntity("/api/cart/" + customerId, Cart.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void testAddItem() {
        UUID customerId = UUID.randomUUID();

        AddItemRequest req = new AddItemRequest();
        req.setProductId(UUID.randomUUID());
        req.setQuantity(2);

        CartItem mockItem = new CartItem();
        mockItem.setId(UUID.randomUUID());
        mockItem.setProductId(req.getProductId());
        mockItem.setQuantity(req.getQuantity());

        Mockito.when(cartService.addItem(eq(customerId), any(AddItemRequest.class)))
                .thenReturn(mockItem);

        ResponseEntity<CartItem> response =
                restTemplate.postForEntity("/api/cart/" + customerId + "/add", req, CartItem.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getProductId()).isEqualTo(req.getProductId());
    }

    @Test
    void testUpdateItemQuantity() {
        UUID customerId = UUID.randomUUID();

        UpdateItemQuantityRequest req = new UpdateItemQuantityRequest();
        req.setProductId(UUID.randomUUID());
        req.setQuantity(5);

        CartItem updated = new CartItem();
        updated.setId(UUID.randomUUID());
        updated.setProductId(req.getProductId());
        updated.setQuantity(req.getQuantity());

        Mockito.when(cartService.updateItemQuantity(eq(customerId), any(UpdateItemQuantityRequest.class)))
                .thenReturn(updated);

        HttpEntity<UpdateItemQuantityRequest> entity = new HttpEntity<>(req);

        ResponseEntity<CartItem> response =
                restTemplate.exchange(
                        "/api/cart/" + customerId + "/update",
                        HttpMethod.PUT,
                        entity,
                        CartItem.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getQuantity()).isEqualTo(5);
    }

    @Test
    void testRemoveItem() {
        UUID customerId = UUID.randomUUID();

        RemoveItemRequest req = new RemoveItemRequest();
        req.setProductId(UUID.randomUUID());

        Cart mockCart = new Cart();
        mockCart.setId(UUID.randomUUID());

        Mockito.when(cartService.removeItem(customerId, req.getProductId()))
                .thenReturn(mockCart);

        HttpEntity<RemoveItemRequest> entity = new HttpEntity<>(req);

        ResponseEntity<Cart> response =
                restTemplate.exchange(
                        "/api/cart/" + customerId + "/remove",
                        HttpMethod.DELETE,
                        entity,
                        Cart.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(mockCart.getId());
    }

    @Test
    void testClearCart() {
        UUID customerId = UUID.randomUUID();

        Mockito.doNothing().when(cartService).clearCart(customerId);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        "/api/cart/" + customerId + "/clear",
                        HttpMethod.DELETE,
                        null,
                        Map.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("message")).isEqualTo("Cart cleared successfully");
    }
}
