package com.gdn.training.cart.controller;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.dto.CartResponse;
import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add-cart")
    public ResponseEntity<Cart> addToCart(@RequestBody AddToCartRequest request, Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.addToCart(username, request);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/view-cart")
    public ResponseEntity<CartResponse> viewCart(Authentication authentication) {
        String username = authentication.getName();
        CartResponse response = cartService.viewCart(username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-from-cart/{productId}")
    public ResponseEntity<Cart> deleteProductFromCart(@PathVariable String productId, Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.deleteProductFromCart(username, productId);
        return ResponseEntity.ok(cart);
    }
}
