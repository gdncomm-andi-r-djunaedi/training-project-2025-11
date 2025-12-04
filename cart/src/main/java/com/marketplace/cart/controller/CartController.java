package com.marketplace.cart.controller;

import com.marketplace.cart.command.AddToCartCommand;
import com.marketplace.cart.command.GetCartCommand;
import com.marketplace.cart.command.RemoveFromCartCommand;
import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.request.AddToCartCommandRequest;
import com.marketplace.cart.dto.request.GetCartRequest;
import com.marketplace.cart.dto.request.RemoveFromCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.common.command.CommandExecutor;
import com.marketplace.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for cart operations
 */
@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final CommandExecutor commandExecutor;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestHeader(USER_ID_HEADER) String userIdHeader,
            @Valid @RequestBody AddToCartRequest request) {

        UUID userId = UUID.fromString(userIdHeader);
        log.info("Add to cart request for user: {}, product: {}", userId, request.getProductId());

        AddToCartCommandRequest commandRequest = AddToCartCommandRequest.builder()
                .userId(userId)
                .addToCartRequest(request)
                .build();

        Cart cart = commandExecutor.execute(AddToCartCommand.class, commandRequest);
        CartResponse response = CartMapper.toCartResponse(cart);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader(USER_ID_HEADER) String userIdHeader) {

        UUID userId = UUID.fromString(userIdHeader);
        log.info("Get cart request for user: {}", userId);

        GetCartRequest request = GetCartRequest.builder().userId(userId).build();
        Cart cart = commandExecutor.execute(GetCartCommand.class, request);
        CartResponse response = CartMapper.toCartResponse(cart);

        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @RequestHeader(USER_ID_HEADER) String userIdHeader,
            @PathVariable String productId) {

        UUID userId = UUID.fromString(userIdHeader);
        log.info("Remove from cart request for user: {}, product: {}", userId, productId);

        RemoveFromCartRequest request = RemoveFromCartRequest.builder()
                .userId(userId)
                .productId(productId)
                .build();

        Cart cart = commandExecutor.execute(RemoveFromCartCommand.class, request);
        CartResponse response = CartMapper.toCartResponse(cart);

        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", response));
    }
}
