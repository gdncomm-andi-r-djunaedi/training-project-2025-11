package com.microservice.cart.controller;

import com.microservice.cart.dto.AddToCartRequestDto;
import com.microservice.cart.dto.CartDto;
import com.microservice.cart.service.CartService;
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
    public ResponseEntity<CartDto> getCart(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("GET /api/cart - Request received for userId: {}", userId);

        CartDto cartDto = cartService.getCart(userId);

        log.info("GET /api/cart - Response sent successfully for userId: {}", userId);

        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddToCartRequestDto request) {

        log.info("POST /api/cart/items - Request received for userId: {}, productId: {}, quantity: {}",
                userId,
                request != null ? request.getProductId() : null,
                request != null ? request.getQuantity() : null);

        CartDto cartDto = cartService.addItemToCart(userId, request);

        log.info("POST /api/cart/items - Item added successfully for userId: {}, returning 201 Created", userId);

        return new ResponseEntity<>(cartDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("DELETE /api/cart/items/{} - Request received for userId: {}", itemId, userId);

        cartService.removeItemFromCart(userId, itemId);

        log.info("DELETE /api/cart/items/{} - Item removed successfully for userId: {}, returning 204 No Content",
                itemId, userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}