package com.example.cart.controller;

import com.example.cart.dto.CartItemRequest;
import com.example.cart.dto.CartItemResponse;
import com.example.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody CartItemRequest request) {
        return cartService.addToCart(userId, request) ?
                ResponseEntity.status(HttpStatus.CREATED).build() :
                ResponseEntity.badRequest()
                        .body("Product Out Of Stock or User Not Found or Product Not Found");
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long productId,
            @RequestHeader("X-User-ID") String userId) {
        return cartService.removeItemFromCart(userId, productId) ?
                ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Product Not Found or User Not Found");
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartItems(
            @RequestHeader("X-User-ID") String userId) {
        return new ResponseEntity<>(cartService.fetchCartByUser(userId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
