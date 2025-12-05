package com.cart.controller;

import com.cart.dto.request.AddItemRequest;
import com.cart.dto.request.RemoveItemRequest;
import com.cart.dto.request.UpdateItemQuantityRequest;
import com.cart.entity.Cart;
import com.cart.entity.CartItem;
import com.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{customerId}")
    public ResponseEntity<Cart> getCartByCustomerId(@PathVariable UUID customerId) {
        Cart response = cartService.getOrCreateCart(customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{customerId}/add")
    public ResponseEntity<CartItem> addItem(
            @PathVariable UUID customerId,
            @RequestBody AddItemRequest request
    ) {
        CartItem response = cartService.addItem(customerId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}/update")
    public ResponseEntity<CartItem> updateItemQuantity(
            @PathVariable UUID customerId,
            @RequestBody UpdateItemQuantityRequest request
    ) {
        CartItem response = cartService.updateItemQuantity(customerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{customerId}/remove")
    public ResponseEntity<Cart> removeItem(
            @PathVariable UUID customerId,
            @RequestBody RemoveItemRequest request
    ) {
        Cart response = cartService.removeItem(customerId, request.getProductId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{customerId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable UUID customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
    }
}
