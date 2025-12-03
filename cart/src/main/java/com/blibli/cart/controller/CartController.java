package com.blibli.cart.controller;

import com.blibli.cart.dto.AddToCartRequest;
import com.blibli.cart.dto.ApiResponse;
import com.blibli.cart.dto.CartResponse;
import com.blibli.cart.exception.BadRequestException;
import com.blibli.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @Valid @RequestBody AddToCartRequest request) {
        
        validateUserId(userId);
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);
        
        CartResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Product added to cart", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        validateUserId(userId);
        log.debug("Fetching cart for user {}", userId);
        
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @PathVariable String productId) {
        
        validateUserId(userId);
        log.info("Removing product {} from cart for user {}", productId, userId);
        
        CartResponse response = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from cart", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        validateUserId(userId);
        log.info("Clearing cart for user {}", userId);
        
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            log.error("Missing or empty X-User-Id header");
            throw new BadRequestException("User authentication required");
        }
        
        // Validate UUID format (basic validation)
        if (!isValidUUID(userId)) {
            log.error("Invalid UUID format in X-User-Id header: {}", userId);
            throw new BadRequestException("Invalid user identifier");
        }
    }

    private boolean isValidUUID(String uuid) {
        // Basic UUID format check: 8-4-4-4-12 hexadecimal characters
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }
}
