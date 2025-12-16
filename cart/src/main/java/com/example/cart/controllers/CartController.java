package com.example.cart.controllers;

import com.example.cart.dto.AddToCartRequestDTO;
import com.example.cart.dto.CartResponseDTO;
import com.example.cart.service.CartService;
import com.example.cart.utils.APIResponse;
import com.example.cart.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<APIResponse<CartResponseDTO>> getCart(@RequestHeader("x-user-id") String userId) {
        APIResponse<CartResponseDTO> response = ResponseUtil.success(
                cartService.getCart(userId)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<APIResponse<String>> addOrUpdateToCart(
            @RequestHeader("x-user-id") String userId,
            @RequestBody AddToCartRequestDTO requestDTO) {
        APIResponse<String> response = ResponseUtil.success(cartService.addToCartOrUpdateQuantity(userId, requestDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<APIResponse<String>> removeItemFromCart(
            @RequestHeader("x-user-id") String userId,
            @PathVariable Long productId) {
        APIResponse<String> response = ResponseUtil.success(cartService.removeItemFromCart(userId, productId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<APIResponse<String>> emptyCart(@RequestHeader("x-user-id") String userId) {
        String message = cartService.emptyCart(userId);
        APIResponse<String> response = ResponseUtil.success(message);
        return ResponseEntity.ok(response);
    }
}
