package com.gdn.training.cart.controller;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gdn.training.cart.service.CartService;
import com.gdn.training.cart.model.request.AddToCartRequest;
import com.gdn.training.cart.model.response.CartResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> addToCart(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody AddToCartRequest cartRequest
    ) {
        String userEmail = jwt.getSubject();
        CartResponse cart = cartService.addToCart(userEmail, cartRequest);

        return ResponseEntity.ok(cart);
    }
    
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userEmail = jwt.getSubject();
        CartResponse cart = cartService.getCart(userEmail);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String productId
    ) {
        String userEmail = jwt.getSubject();
        CartResponse cart = cartService.removeFromCart(userEmail, productId);
        return ResponseEntity.ok(cart);
    }
}
