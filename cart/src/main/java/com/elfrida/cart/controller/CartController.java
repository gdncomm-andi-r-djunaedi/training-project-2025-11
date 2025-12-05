package com.elfrida.cart.controller;

import com.elfrida.cart.model.Cart;
import com.elfrida.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<Cart> addToCart(
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        Cart cart = cartService.addToCart(email, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.getCart(email));
    }

    @DeleteMapping("/items")
    public ResponseEntity<Cart> deleteItem(
            @RequestParam String productId,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        Cart cart = cartService.removeItem(email, productId);
        return ResponseEntity.ok(cart);
    }
}


