package com.gdn.marketplace.cart.controller;

import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.entity.CartItem;
import com.gdn.marketplace.cart.service.CartService;
import com.gdn.marketplace.cart.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService service;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public Cart addToCart(@RequestHeader("Authorization") String token, @RequestBody CartItem item) {
        String username = extractUsername(token);
        return service.addToCart(username, item);
    }

    @GetMapping
    public Cart getCart(@RequestHeader("Authorization") String token) {
        String username = extractUsername(token);
        return service.getCart(username);
    }

    @DeleteMapping("/{productId}")
    public Cart removeFromCart(@RequestHeader("Authorization") String token, @PathVariable String productId) {
        String username = extractUsername(token);
        return service.removeFromCart(username, productId);
    }

    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.extractUsername(token);
    }
}
