package com.marketplace.cart.controller;

import com.marketplace.cart.dto.AddItemRequest;
import com.marketplace.cart.dto.CartResponse;
import com.marketplace.cart.service.CartService;
import com.marketplace.cart.util.ApiResponse;
import com.marketplace.cart.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@RequestHeader("X-User-Id") String userId) {
        CartResponse response = cartService.getCart(userId);
        return ResponseUtil.success(response, "Cart retrieved successfully");

    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddItemRequest request,
            @RequestParam(value = "action", defaultValue = "set") String action) {
        CartResponse response = cartService.addItem(userId, request, action);
        String message = "set".equalsIgnoreCase(action) ? "Item added to cart successfully"
                : "Quantity " + action + "d successfully";
        return ResponseUtil.success(response, message);
    }

    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("productId") String productId) {
        CartResponse response = cartService.removeItem(userId, productId);
        return ResponseUtil.success(response, "Item removed from cart successfully");
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @RequestHeader("X-User-Id") String userId) {
        CartResponse response = cartService.clearCart(userId);
        return ResponseUtil.success(response, "Cart cleared successfully");
    }
}
