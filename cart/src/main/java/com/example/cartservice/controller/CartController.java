package com.example.cartservice.controller;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.CartDTO;
import com.example.cartservice.entity.Cart;
import com.example.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService service;

    @GetMapping
    public CartDTO getCart(@RequestHeader("X-User-Id") Long userId) {
        return service.getCart(userId);
    }

    @PostMapping
    public Cart addToCart(@RequestHeader("X-User-Id") Long userId, 
                         @RequestBody AddToCartRequest request) {
        return service.addToCart(userId, request);
    }

    @DeleteMapping("/{productId}")
    public Cart removeFromCart(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable String productId) {
        return service.removeFromCart(userId, productId);
    }
}
