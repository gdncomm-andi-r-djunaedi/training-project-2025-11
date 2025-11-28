package com.gdn.marketplace.cart.controller;

import com.gdn.marketplace.cart.dto.AddToCartRequest;
import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Cart> addToCart(Principal principal, @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(principal.getName(), request));
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(Principal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Cart> removeFromCart(Principal principal, @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeFromCart(principal.getName(), productId));
    }
}
