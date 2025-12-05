package com.example.cart.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.example.cart.repository.CartRepository;
import com.example.cart.model.Cart;
import java.util.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        return cartRepository.findByUserId(userId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Cart> upsertCart(@PathVariable Long userId, @RequestBody Map<String,Object> body) {
        String items = body.getOrDefault("items", "[]").toString();
        Cart cart = cartRepository.findByUserId(userId).orElse(new Cart(null, userId, items));
        cart.setItemsJson(items);
        return ResponseEntity.ok(cartRepository.save(cart));
    }
}
