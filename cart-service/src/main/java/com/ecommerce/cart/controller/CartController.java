package com.ecommerce.cart.controller;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    CartService cartService;

    @GetMapping("/{memberId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long memberId) {
        return ResponseEntity.ok(cartService.getCartByMemberId(memberId));
    }

    @PostMapping("/{memberId}/items")
    public ResponseEntity<Cart> addItem(@PathVariable Long memberId,
            @RequestParam String productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addItemToCart(memberId, productId, quantity));
    }

    @DeleteMapping("/{memberId}/items/{productId}")
    public ResponseEntity<Cart> removeItem(@PathVariable Long memberId,
            @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(memberId, productId));
    }
}
