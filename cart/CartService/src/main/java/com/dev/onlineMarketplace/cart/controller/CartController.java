package com.dev.onlineMarketplace.cart.controller;

import com.dev.onlineMarketplace.cart.dto.AddToCartRequest;
import com.dev.onlineMarketplace.cart.dto.GdnResponseData;
import com.dev.onlineMarketplace.cart.model.Cart;
import com.dev.onlineMarketplace.cart.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    // Helper to extract user ID from token (mocked for now as Gateway handles auth)
    // In a real scenario, we might parse the JWT or expect a header from Gateway
    private String getUserIdFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (token == null || token.isEmpty()) {
                return "anonymous";
            }
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return token; // Fallback to raw token if not a valid JWT format
            }
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(payload);
            if (node.has("sub")) {
                return node.get("sub").asText();
            }
        } catch (Exception e) {
            log.error("Error parsing token", e);
        }
        return token;
    }

    // Helper to extract Authorization header from request
    private String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    @GetMapping("/getCartItems")
    public ResponseEntity<GdnResponseData<Cart>> getCart(HttpServletRequest request) {
        String token = getAuthorizationHeader(request);
        String userId = getUserIdFromToken(token);
        log.info("Request to get cart for user: {}", userId);
        Cart cart = cartService.getCart(userId);
        return ResponseEntity.ok(GdnResponseData.success(cart, "Cart retrieved successfully"));
    }

    @PostMapping("/addItemToCart")
    public ResponseEntity<GdnResponseData<Cart>> addToCart(
            HttpServletRequest request,
            @RequestBody AddToCartRequest addToCartRequest) {
        String token = getAuthorizationHeader(request);
        String userId = getUserIdFromToken(token);
        log.info("Request to add item to cart for user: {}, productId: {}, quantity: {}", 
                userId, addToCartRequest.getProductId(), addToCartRequest.getQuantity());
        Cart cart = cartService.addToCart(userId, addToCartRequest.getProductId(), addToCartRequest.getQuantity());
        return ResponseEntity.ok(GdnResponseData.success(cart, "Item added to cart"));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<GdnResponseData<Cart>> removeFromCart(
            HttpServletRequest request,
            @PathVariable String productId) {
        String token = getAuthorizationHeader(request);
        String userId = getUserIdFromToken(token);
        log.info("Request to remove item {} from cart for user: {}", productId, userId);
        Cart cart = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(GdnResponseData.success(cart, "Item removed from cart"));
    }
}
