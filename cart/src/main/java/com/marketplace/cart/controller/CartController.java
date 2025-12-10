package com.marketplace.cart.controller;

import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.CartDto;
import com.marketplace.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;
    
    @PostMapping("/items")
    public ResponseEntity<CartDto> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Add to cart request for user: {}", userId);
        CartDto cart = cartService.addToCart(userId, request);
        return new ResponseEntity<>(cart, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<CartDto> getCart(@RequestHeader("X-User-Id") String userId) {
        log.info("Get cart request for user: {}", userId);
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeFromCart(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId) {
        log.info("Remove from cart request for user: {}, product: {}", userId, productId);
        CartDto cart = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart(@RequestHeader("X-User-Id") String userId) {
        log.info("Clear cart request for user: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
    }
}
