package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<Cart> addToCart(@Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Cart cart = cartService.addToCart(userDetails.getUsername(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Cart cart = cartService.getCart(userDetails.getUsername());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.removeFromCart(userDetails.getUsername(), itemId);
        return ResponseEntity.ok().build();
    }
}
