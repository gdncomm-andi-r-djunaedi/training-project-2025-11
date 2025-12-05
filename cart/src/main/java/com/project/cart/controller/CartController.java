package com.project.cart.controller;

import com.project.cart.dto.request.AddCartItemRequest;
import com.project.cart.dto.request.UpdateCartItemRequest;
import com.project.cart.dto.response.CartCountResponse;
import com.project.cart.dto.response.CartResponse;
import com.project.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Shopping Cart operations
 */
@Slf4j
@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @Operation(summary = "Add item to cart",
            description = "Adds a product to the user's shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or cart limit exceeded"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request) {

        String userId = authentication.getName();
        log.info("REST request to add item to cart: userId={}", userId);

        CartResponse response = cartService.addItem(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update item quantity",
            description = "Updates the quantity of a product in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found in cart")
    })
    public ResponseEntity<CartResponse> updateItemQuantity(
            Authentication authentication,
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        String userId = authentication.getName();
        log.info("REST request to update item quantity: userId={}, productId={}",
                userId, productId);

        CartResponse response = cartService.updateItemQuantity(userId, productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart",
            description = "Removes a product from the shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found in cart")
    })
    public ResponseEntity<CartResponse> removeItem(
            Authentication authentication,
            @Parameter(description = "Product ID") @PathVariable String productId) {

        String userId = authentication.getName();
        log.info("REST request to remove item from cart: userId={}, productId={}",
                userId, productId);

        CartResponse response = cartService.removeItem(userId, productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get shopping cart",
            description = "Retrieves the user's complete shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        String userId = authentication.getName();
        log.info("REST request to get cart: userId={}", userId);

        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(summary = "Clear cart",
            description = "Removes all items from the shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        String userId = authentication.getName();
        log.info("REST request to clear cart: userId={}", userId);

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Get cart item count",
            description = "Returns the total number of items in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CartCountResponse> getCartCount(Authentication authentication) {
        String userId = authentication.getName();
        log.info("REST request to get cart count: userId={}", userId);

        CartCountResponse response = cartService.getCartCount(userId);
        return ResponseEntity.ok(response);
    }
}
