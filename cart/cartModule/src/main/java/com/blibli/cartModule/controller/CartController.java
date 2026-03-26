package com.blibli.cartModule.controller;

import com.blibli.cartModule.dto.*;
import com.blibli.cartModule.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Cart Management", description = "APIs for managing cart")
@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "Add item to cart", description = "Add a product to the cart")
    @PostMapping("/addItems")
    public ResponseEntity<ApiResponse<CartResponseDto>> addItem(
            @RequestHeader("Member-Id") Long memberId, @RequestBody AddItemRequestDto request) {
        log.info("POST /api/carts/addItems - memberId: {}, productId: {}, quantity: {}", memberId,
                request.getProductId(), request.getQuantity());
        CartResponseDto cartResponseDto = cartService.addItem(memberId, request);
        return new ResponseEntity<>(ApiResponse.success(cartResponseDto), HttpStatus.OK);
    }

    @Operation(summary = "Remove item from cart", description = "Remove a product from the cart by quantity")
    @DeleteMapping("/removeItems")
    public ResponseEntity<ApiResponse<CartResponseDto>> removeItem(
            @RequestHeader("Member-Id") Long memberId, @RequestBody RemoveItemDto request) {
        log.info("DELETE /api/carts/removeItems - memberId: {}, productId: {}, quantity: {}", 
                memberId, request.getProductId(), request.getQuantity());
        CartResponseDto cartResponseDto = cartService.removeItem(memberId, request);
        return new ResponseEntity<>(ApiResponse.success(cartResponseDto), HttpStatus.OK);
    }

    @Operation(summary = "Get cart", description = "Get the cart for a member")
    @GetMapping("/getCart")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(
            @RequestHeader("Member-Id") Long memberId) {
        log.info("GET /api/carts/getCart - memberId: {}", memberId);
        CartResponseDto cartResponseDto = cartService.getCart(memberId);
        return new ResponseEntity<>(ApiResponse.success(cartResponseDto), HttpStatus.OK);
    }

    @Operation(summary = "Clear cart", description = "Clear the cart for a member")
    @DeleteMapping("/clearCart")
    public ResponseEntity<ApiResponse<String>> clearCart(@RequestHeader("Member-Id") Long memberId) {
        log.info("DELETE /api/carts/clearCart - memberId: {}", memberId);
        cartService.clearCart(memberId);
        return new ResponseEntity<>(ApiResponse.success("Cart cleared successfully"), HttpStatus.OK);
    }

}