package com.blibli.gdn.cartService.web.controller;

import com.blibli.gdn.cartService.model.Cart;
import com.blibli.gdn.cartService.model.CartItem;
import com.blibli.gdn.cartService.service.CartService;
import com.blibli.gdn.cartService.web.model.AddToCartRequest;
import com.blibli.gdn.cartService.web.model.CartResponseDTOs;
import com.blibli.gdn.cartService.web.model.GdnResponseData;
import com.blibli.gdn.cartService.web.model.UpdateQuantityRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private String getMemberId(HttpServletRequest request) {
        String memberId = request.getHeader("X-User-Id");
        if (memberId == null || memberId.isEmpty()) {
            return "guest-" + UUID.randomUUID().toString();
        }
        return memberId;
    }

    @PostMapping
    public ResponseEntity<GdnResponseData<CartResponseDTOs.AddToCartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {

        String memberId = getMemberId(httpRequest);
        String traceId = UUID.randomUUID().toString();

        log.info("Received request to add to cart for member: {}, traceId: {}", memberId, traceId);

        Cart cart = cartService.addToCart(memberId, request);

        CartResponseDTOs.AddToCartResponse data = CartResponseDTOs.AddToCartResponse.builder()
                .message("Item added")
                .sku(request.getSku())
                .qty(request.getQty())
                .cart(cart)
                .build();

        GdnResponseData<CartResponseDTOs.AddToCartResponse> response = GdnResponseData.<CartResponseDTOs.AddToCartResponse>builder()
                .success(true)
                .data(data)
                .message("Item added to cart successfully")
                .traceId(traceId)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<GdnResponseData<Cart>> getCart(HttpServletRequest httpRequest) {
        String memberId = getMemberId(httpRequest);
        String traceId = UUID.randomUUID().toString();

        log.info("Received request to get cart for member: {}, traceId: {}", memberId, traceId);

        Cart cart = cartService.getCart(memberId);

        GdnResponseData<Cart> response = GdnResponseData.<Cart>builder()
                .success(true)
                .data(cart)
                .message("Cart retrieved successfully")
                .traceId(traceId)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/item/{sku}")
    public ResponseEntity<GdnResponseData<CartResponseDTOs.UpdateQuantityResponse>> updateQuantity(
            @PathVariable String sku,
            @Valid @RequestBody UpdateQuantityRequest request,
            HttpServletRequest httpRequest) {

        String memberId = getMemberId(httpRequest);
        String traceId = UUID.randomUUID().toString();

        log.info("Received request to update quantity for member: {}, sku: {}, traceId: {}",
                memberId, sku, traceId);

        Cart cart = cartService.updateQuantity(memberId, sku, request);

        CartResponseDTOs.UpdateQuantityResponse data = CartResponseDTOs.UpdateQuantityResponse.builder()
                .message("Quantity updated")
                .sku(sku)
                .qty(request.getQty())
                .cart(cart)
                .build();

        GdnResponseData<CartResponseDTOs.UpdateQuantityResponse> response = GdnResponseData.<CartResponseDTOs.UpdateQuantityResponse>builder()
                .success(true)
                .data(data)
                .message("Quantity updated successfully")
                .traceId(traceId)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<GdnResponseData<CartResponseDTOs.RemoveItemResponse>> removeItem(
            @PathVariable String sku,
            HttpServletRequest httpRequest) {

        String memberId = getMemberId(httpRequest);
        String traceId = UUID.randomUUID().toString();

        log.info("Received request to remove item {} for member: {}, traceId: {}",
                sku, memberId, traceId);

        cartService.removeItem(memberId, sku);
        Cart cart = cartService.getCart(memberId);

        CartResponseDTOs.RemoveItemResponse data = CartResponseDTOs.RemoveItemResponse.builder()
                .message("Item removed")
                .sku(sku)
                .cart(cart)
                .build();

        GdnResponseData<CartResponseDTOs.RemoveItemResponse> response = GdnResponseData.<CartResponseDTOs.RemoveItemResponse>builder()
                .success(true)
                .data(data)
                .message("Item removed successfully")
                .traceId(traceId)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<GdnResponseData<String>> clearCart(HttpServletRequest httpRequest) {
        String memberId = getMemberId(httpRequest);
        String traceId = UUID.randomUUID().toString();

        log.info("Received request to clear cart for member: {}, traceId: {}", memberId, traceId);

        cartService.clearCart(memberId);

        GdnResponseData<String> response = GdnResponseData.<String>builder()
                .success(true)
                .data("Cart cleared")
                .message("Cart cleared successfully")
                .traceId(traceId)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/merge")
    public ResponseEntity<GdnResponseData<String>> mergeCarts(
            @RequestParam String guestCartId,
            HttpServletRequest httpRequest) {

        String memberId = httpRequest.getHeader("X-User-Id");
        String traceId = UUID.randomUUID().toString();

        if (memberId == null || memberId.isEmpty()) {
            log.warn("Merge request without X-User-Id header, traceId: {}", traceId);
            GdnResponseData<String> response = GdnResponseData.<String>builder()
                    .success(false)
                    .data(null)
                    .message("User ID is required for cart merge")
                    .traceId(traceId)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        log.info("Received request to merge guest cart {} to member: {}, traceId: {}",
                guestCartId, memberId, traceId);

        cartService.mergeCarts(guestCartId, memberId);

        GdnResponseData<String> response = GdnResponseData.<String>builder()
                .success(true)
                .data("Carts merged successfully")
                .message("Guest cart merged into user cart")
                .traceId(traceId)
                .build();

        return ResponseEntity.ok(response);
    }
}
