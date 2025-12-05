package com.training.cart.controller;

import com.training.cart.dto.AddToCartRequest;
import com.training.cart.dto.CartResponse;
import com.training.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        String customerEmail = authentication.getName();
        CartResponse response = cartService.addToCart(customerEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        String customerEmail = authentication.getName();
        CartResponse response = cartService.getCart(customerEmail);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable Long itemId,
            Authentication authentication) {
        String customerEmail = authentication.getName();
        cartService.removeFromCart(customerEmail, itemId);
        return ResponseEntity.noContent().build();
    }
}
