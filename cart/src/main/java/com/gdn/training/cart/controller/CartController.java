package com.gdn.training.cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.service.CartService;
import com.gdn.training.common.model.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Cart management endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieve the current user's shopping cart")
    public ResponseEntity<BaseResponse<Cart>> getCart(@RequestHeader("X-User-Id") String userId) {
        log.info("Retrieving cart for user {}", userId);
        return ResponseEntity.ok(BaseResponse.success(cartService.getCart(userId)));
    }

    @PostMapping
    @Operation(summary = "Add to cart", description = "Add an item to the shopping cart")
    public ResponseEntity<BaseResponse<Cart>> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CartItem item
    ) {
        log.info("Adding product {} qty {} to cart for user {}", item.getProductId(), item.getQuantity(), userId);
        return ResponseEntity.ok(BaseResponse.success("Item added to cart", cartService.addToCart(userId, item)));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove from cart", description = "Remove an item from the shopping cart")
    public ResponseEntity<BaseResponse<Cart>> removeFromCart(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId
    ) {
        log.info("Removing product {} from cart for user {}", productId, userId);
        return ResponseEntity.ok(BaseResponse.success("Item removed from cart", cartService.removeFromCart(userId, productId)));
    }
}
