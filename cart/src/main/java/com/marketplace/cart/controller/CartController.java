package com.marketplace.cart.controller;

import com.marketplace.cart.command.AddToCartCommand;
import com.marketplace.cart.command.GetCartCommand;
import com.marketplace.cart.command.RemoveFromCartCommand;
import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.request.AddToCartCommandRequest;
import com.marketplace.cart.dto.request.GetCartRequest;
import com.marketplace.cart.dto.request.RemoveFromCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.common.controller.BaseCommandController;
import com.marketplace.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * REST controller for cart operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController extends BaseCommandController {

        private static final String USER_ID_HEADER = "X-User-Id";

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

                CartResponse response = execute(AddToCartCommand.class, commandRequest);
                return createdResponse("Item added to cart", response);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<CartResponse>> getCart(
                        @RequestHeader(USER_ID_HEADER) String userIdHeader) {

                UUID userId = UUID.fromString(userIdHeader);
                log.info("Get cart request for user: {}", userId);

                GetCartRequest request = GetCartRequest.builder().userId(userId).build();
                CartResponse response = execute(GetCartCommand.class, request);
                return okResponse("Cart retrieved successfully", response);
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

                CartResponse response = execute(RemoveFromCartCommand.class, request);
                return okResponse("Item removed from cart", response);
        }
}
