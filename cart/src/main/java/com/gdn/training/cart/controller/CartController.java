package com.gdn.training.cart.controller;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.dto.GetCartResponse;
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
    public ResponseEntity<Cart> addToCart(
            @RequestHeader("X-User-Name") String username,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestBody AddToCartRequest request) {
        if (memberId != null) {
            request.setMemberId(memberId);
        }
        return ResponseEntity.ok(cartService.addToCart(username, request));
    }

    @GetMapping
    public ResponseEntity<GetCartResponse> getCart(@RequestHeader("X-User-Name") String username) {
        return ResponseEntity.ok(cartService.getCart(username));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(
            @RequestHeader("X-User-Name") String username,
            @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeFromCart(username, productId));
    }
}
