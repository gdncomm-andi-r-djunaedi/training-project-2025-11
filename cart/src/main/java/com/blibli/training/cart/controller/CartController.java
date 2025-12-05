package com.blibli.training.cart.controller;

import com.blibli.training.cart.dto.CartResponse;
import com.blibli.training.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add-to-bag")
    public ResponseEntity<CartResponse> addToBag(
            @RequestParam String username,
            @RequestParam String productName,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.addToBag(username, productName, quantity));
    }

    @GetMapping("/{username}")
    public ResponseEntity<CartResponse> getAllCart(@PathVariable String username) {
        return ResponseEntity.ok(cartService.getAllCart(username));
    }
}
