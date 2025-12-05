package com.blublu.cart.controller;

import com.blublu.cart.document.CartDocument;
import com.blublu.cart.interfaces.CartService;
import com.blublu.cart.model.request.EditQtyRequest;
import com.blublu.cart.model.response.CartResponse;
import com.blublu.cart.model.response.GenericBodyResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    void getCartItems_Success() {
        String username = "testUser";
        CartResponse mockCart = CartResponse.builder()
                .username(username)
                .items(Collections.emptyList())
                .build();

        when(cartService.getUserCart(username)).thenReturn(mockCart);

        ResponseEntity<?> response = cartController.getCartItems(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(1, body.getContent().size());
        assertEquals(mockCart, body.getContent().get(0));
    }

    @Test
    void getCartItems_Failure() {
        String username = "testUser";

        when(cartService.getUserCart(username)).thenReturn(null);

        ResponseEntity<?> response = cartController.getCartItems(username);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Cart not found", body.getErrorMessage());
    }

    @Test
    void addItemToCart_Success() {
        String username = "testUser";
        CartDocument.Item item = new CartDocument.Item();
        item.setSkuCode("SKU123");
        item.setQuantity(1);

        when(cartService.addItemToCart(eq(username), any(CartDocument.Item.class))).thenReturn(true);

        ResponseEntity<?> response = cartController.addItemToCart(username, item);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
    }

    @Test
    void addItemToCart_Failure() {
        String username = "testUser";
        CartDocument.Item item = new CartDocument.Item();
        item.setSkuCode("SKU123");
        item.setQuantity(1);

        when(cartService.addItemToCart(eq(username), any(CartDocument.Item.class))).thenReturn(false);

        ResponseEntity<?> response = cartController.addItemToCart(username, item);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("SKU not found!", body.getErrorMessage());
    }

    @Test
    void deleteCart_Success() {
        String username = "testUser";

        when(cartService.clearCart(username)).thenReturn(true);

        ResponseEntity<?> response = cartController.deleteCart(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
    }

    @Test
    void deleteCart_Failure() {
        String username = "testUser";

        when(cartService.clearCart(username)).thenReturn(false);

        ResponseEntity<?> response = cartController.deleteCart(username);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Cart not found!", body.getErrorMessage());
    }

    @Test
    void editCartItem_Success() {
        String username = "testUser";
        EditQtyRequest request = EditQtyRequest.builder()
                .skuCode("SKU123")
                .newQty(5)
                .build();

        when(cartService.editCartItem(eq(username), any(EditQtyRequest.class))).thenReturn(true);

        ResponseEntity<?> response = cartController.editCartItem(username, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
    }

    @Test
    void editCartItem_Failure() {
        String username = "testUser";
        EditQtyRequest request = EditQtyRequest.builder()
                .skuCode("SKU123")
                .newQty(5)
                .build();

        when(cartService.editCartItem(eq(username), any(EditQtyRequest.class))).thenReturn(false);

        ResponseEntity<?> response = cartController.editCartItem(username, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("User cart or item does not exist!", body.getErrorMessage());
    }

    @Test
    void deleteItemFromCart_Success() {
        String username = "testUser";
        String skuCode = "SKU123";

        when(cartService.removeItemFromCart(username, skuCode)).thenReturn(true);

        ResponseEntity<?> response = cartController.deleteItemFromCart(username, skuCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
    }

    @Test
    void deleteItemFromCart_Failure() {
        String username = "testUser";
        String skuCode = "SKU123";

        when(cartService.removeItemFromCart(username, skuCode)).thenReturn(false);

        ResponseEntity<?> response = cartController.deleteItemFromCart(username, skuCode);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse<?> body = (GenericBodyResponse<?>) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Item not found in user cart!", body.getErrorMessage());
    }
}
