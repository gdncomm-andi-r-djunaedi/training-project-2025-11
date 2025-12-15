package com.gdn.training.cart_service.controller;

import com.gdn.training.cart_service.dto.AddToCartRequest;
import com.gdn.training.cart_service.dto.CartResponse;
import com.gdn.training.cart_service.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping Cart Management APIs")
public class CartController {

    private final CartService cartService;

    //POST - add item to cart
    @PostMapping("/items")
    @Operation(summary = "add item to cart", description = "add product to shopping cart")
    public ResponseEntity<CartResponse> addToCart(
            @Parameter(description = "Member ID from JWT token")
            @RequestHeader("X-Member-Id") Long memberId,

            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //GET - Current Cart
    @GetMapping
    @Operation(summary = "get items from current cart")
    public ResponseEntity<CartResponse> getCart(
            @Parameter(description = "Member ID from JWT token")
            @RequestHeader("X-Member-Id") Long memberId
    ){
        CartResponse response = cartService.getCart(memberId);
        return ResponseEntity.ok(response);
    }

    //DELETE - delete item from cart
    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeFromCart(
            @Parameter(description = "Member ID from JWT Token")
            @RequestHeader("X-Member-Id") Long memberId,

            @Parameter(description = "Product ID to remove")
            @PathVariable Long productId){
        CartResponse response = cartService.removeFromCart(memberId, productId);
        return ResponseEntity.ok(response);
    }

    //DELETE - clear all items in cart
    @DeleteMapping
    @Operation(summary = "clear cart")
    public ResponseEntity<Void> clearCart(
            @Parameter(description = "Member ID from JWT token")
            @RequestHeader("X-Member-Id") Long memberId
    ){
            cartService.clearCart(memberId);
            return ResponseEntity.noContent().build();
    }




}
