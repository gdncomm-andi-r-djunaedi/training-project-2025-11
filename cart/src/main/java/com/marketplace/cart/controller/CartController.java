package com.marketplace.cart.controller;

import com.marketplace.cart.command.AddToCartCommand;
import com.marketplace.cart.command.CartCommand;
import com.marketplace.cart.command.RemoveFromCartCommand;
import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.command.CommandInvoker;
import com.marketplace.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Cart controller handles shopping cart operations
 * Expects X-User-Id header from API Gateway (after JWT validation)
 */
@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CommandInvoker commandInvoker;

    private static final String USER_ID_HEADER = "X-User-Id";

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestHeader(USER_ID_HEADER) String userIdHeader,
            @Valid @RequestBody AddToCartRequest request) {

        UUID userId = UUID.fromString(userIdHeader);
        log.info("Add to cart request for user: {}, product: {}", userId, request.getProductId());

        CartCommand command = new AddToCartCommand(cartService, userId, request);
        Cart cart = commandInvoker.executeCommand(command);
        CartResponse response = CartMapper.toCartResponse(cart);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader(USER_ID_HEADER) String userIdHeader) {

        UUID userId = UUID.fromString(userIdHeader);
        log.info("Get cart request for user: {}", userId);

        Cart cart = cartService.getCart(userId);
        CartResponse response = CartMapper.toCartResponse(cart);

        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @RequestHeader(USER_ID_HEADER) String userIdHeader,
            @PathVariable String productId) {

        UUID userId = UUID.fromString(userIdHeader);
        log.info("Remove from cart request for user: {}, product: {}", userId, productId);

        CartCommand command = new RemoveFromCartCommand(cartService, userId, productId);
        Cart cart = commandInvoker.executeCommand(command);
        CartResponse response = CartMapper.toCartResponse(cart);

        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", response));
    }
}
