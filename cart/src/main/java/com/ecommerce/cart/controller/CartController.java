package com.ecommerce.cart.controller;

import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    record AddToCartRequest(String productId, Integer quantity) {
    }

    @PostMapping
    public ResponseEntity<String> addToCart(@RequestHeader("X-User-Id") String username,
            @RequestBody AddToCartRequest payload) {
        String productId = payload.productId();
        Integer quantity = payload.quantity();

        cartService.addToCart(username, productId, quantity);
        return ResponseEntity.ok("Added to cart");
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> viewCart(@RequestHeader("X-User-Id") String username) {
        return ResponseEntity.ok(cartService.viewCart(username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFromCart(@RequestHeader("X-User-Id") String username, @PathVariable Long id) {
        cartService.deleteFromCart(username, id);
        return ResponseEntity.ok("Deleted from cart");
    }
}
