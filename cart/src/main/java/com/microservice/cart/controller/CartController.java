package com.microservice.cart.controller;

import com.microservice.cart.dto.AddToCartRequestDto;
import com.microservice.cart.dto.CartDto;
import com.microservice.cart.service.CartService;
import com.microservice.cart.wrapper.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /api/cart - Request received for userId: {}", userId);

        CartDto cartDto = cartService.getCart(userId);
        ApiResponse<CartDto> response = ApiResponse.success(cartDto, HttpStatus.OK);

        log.info("GET /api/cart - Response sent successfully for userId: {}", userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Boolean>> addItemToCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddToCartRequestDto request) {
        log.info("POST /api/cart/items - Request received for userId: {}, productId: {}, quantity: {}",
                userId,
                request != null ? request.getSkuId() : null,
                request != null ? request.getQuantity() : null);

        Boolean addedToCart = cartService.addItemToCart(userId, request);
        ApiResponse<Boolean> response = ApiResponse.success(addedToCart, HttpStatus.CREATED);

        log.info("POST /api/cart/items - Item added successfully for userId: {}, returning 201 Created", userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Boolean>> removeItemFromCart(
            @PathVariable String itemId,  // Changed from Long itemId to String itemId
            @RequestHeader("X-User-Id") Long userId) {
        log.info("DELETE /api/cart/items/{} - Request received for userId: {}", itemId, userId);

        cartService.removeItemFromCart(userId, itemId);
        ApiResponse<Boolean> response = ApiResponse.success(true, HttpStatus.NO_CONTENT);

        log.info("DELETE /api/cart/items/{} - Item removed successfully for userId: {}, returning 204 No Content",
                itemId, userId);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

}