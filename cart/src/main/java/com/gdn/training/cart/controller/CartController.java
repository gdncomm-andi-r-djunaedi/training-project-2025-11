package com.gdn.training.cart.controller;

import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.service.CartService;
import com.gdn.training.common.model.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieve the current user's shopping cart")
    public ResponseEntity<BaseResponse<Cart>> getCart(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(BaseResponse.success(cartService.getCart(userId)));
    }

    @PostMapping
    @Operation(summary = "Add to cart", description = "Add an item to the shopping cart")
    public ResponseEntity<BaseResponse<Cart>> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CartItem item
    ) {
        return ResponseEntity.ok(BaseResponse.success("Item added to cart", cartService.addToCart(userId, item)));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove from cart", description = "Remove an item from the shopping cart")
    public ResponseEntity<BaseResponse<Cart>> removeFromCart(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(BaseResponse.success("Item removed from cart", cartService.removeFromCart(userId, productId)));
    }
}
