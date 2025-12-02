package com.gdn.training.cart.controller;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long memberId) {
        return ResponseEntity.ok(cartService.getCart(memberId));
    }

    @DeleteMapping("/{memberId}/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable Long memberId, @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeFromCart(memberId, productId));
    }
}
