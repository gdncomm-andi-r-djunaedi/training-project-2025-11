package com.blibli.apigateway.controller;

import com.blibli.apigateway.client.CartClient;
import com.blibli.apigateway.dto.request.CartDto;
import com.blibli.apigateway.dto.response.CartResponseDto;
import com.blibli.apigateway.dto.response.ViewCartResponseDto;
import com.blibli.apigateway.exception.TokenValidationException;
import com.blibli.apigateway.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart API")
public class CartController {
    private final CartClient cartClient;
    private final JwtService jwtService;

    public CartController(CartClient cartClient, JwtService jwtService) {
        this.cartClient = cartClient;
        this.jwtService = jwtService;
    }

    private String extractTokenFromHeader(String authHeader, String path) {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new RuntimeException("Missing Authorization header. Please include: Authorization: Bearer <token>");
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header format. Expected: Bearer <token>, but got: " + authHeader);
        }
        
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new RuntimeException("Token is empty after Bearer prefix");
        }

        if (jwtService.isTokenBlacklisted(token)) {
            log.error("Token is blacklisted (user logged out)");
            throw new TokenValidationException("BLACKLISTED", path);
        }
        
        if (jwtService.isTokenExpired(token)) {
            log.error("Token has expired");
            throw new TokenValidationException("EXPIRED", path);
        }
        
        if (!jwtService.validateToken(token)) {
            log.error("Token validation failed");
            throw new TokenValidationException("INVALID", path);
        }
        
        log.info("Token validated successfully");
        return token;
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart", description = "Add a product to the shopping cart",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<?> addToCart(
            @RequestBody CartDto cartDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Adding to cart - Authorization header present: {}", authHeader != null);
        
        if (authHeader == null) {
            throw new RuntimeException("Missing Authorization header. Please include: Authorization: Bearer <token>");
        }
        
        String tokenId = extractTokenFromHeader(authHeader, "/api/cart/add");
        log.info("Adding to cart - productId: {}, quantity: {}", cartDto.getProductId(), cartDto.getQuantity());
        log.info("Passing token to cart service");
        
        CartResponseDto response = cartClient.addToCart(cartDto, "Bearer " + tokenId);
        log.info("=== Cart Service Response ===");
        log.info("Message: {}", response.getMessage());
        log.info("Status: {}", response.getStatus());
        log.info("Product Code: {}", response.getProductCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear")
    @Operation(summary = "Clear cart", description = "Clear all items from the shopping cart",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<?> clearCart(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null) {
            throw new RuntimeException("Missing Authorization header. Please include: Authorization: Bearer <token>");
        }
        
        String tokenId = extractTokenFromHeader(authHeader, "/api/cart/clear");
        log.info("Clearing cart with tokenId, passing to cart service");
        
        String message = cartClient.clearCart("Bearer " + tokenId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/view")
    @Operation(summary = "View cart", description = "View all items in the shopping cart",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<?> viewCart(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("===View Cart Request ===");
        log.info("Authorization header (first 50 chars): {}", authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) : "null");
        log.info("Authorization header length: {}", authHeader != null ? authHeader.length() : 0);
        
        if (authHeader == null) {
            log.error("Authorization header is null!");
            throw new RuntimeException("Missing Authorization header. Please include: Authorization: Bearer <token>");
        }
        
        String tokenId = extractTokenFromHeader(authHeader, "/api/cart/view");
        log.info("Successfully extracted tokenId, passing to cart service as Authorization: Bearer <token>");
        
        ViewCartResponseDto cartResponse = cartClient.viewCart("Bearer " + tokenId);
        return ResponseEntity.ok(cartResponse);
    }
}

