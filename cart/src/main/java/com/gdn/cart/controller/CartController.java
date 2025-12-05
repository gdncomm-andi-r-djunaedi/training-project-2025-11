package com.gdn.cart.controller;

import com.gdn.cart.dto.request.AddCartItemRequestDTO;
import com.gdn.cart.dto.response.ApiResponse;
import com.gdn.cart.dto.request.CartDTO;
import com.gdn.cart.dto.response.ErrorResponseDTO;
import com.gdn.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/addItems")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            @RequestParam("memberId") String memberId,
            @RequestBody AddCartItemRequestDTO request) {
        log.info("Add item to cart, memberId={}, request={}", memberId, request);
        CartDTO cart = cartService.addItem(memberId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Item added to cart", cart)
        );
    }


    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> deleteItem(
            @RequestParam("memberId") String memberId,
            @PathVariable String productId) {
        log.info("Delete item from cart, memberId={}, productId={}", memberId, productId);
        cartService.deleteItem(memberId, productId);
        ErrorResponseDTO status = new ErrorResponseDTO("Item removed from cart");
        return ResponseEntity.ok(
                ApiResponse.success("Item removed from cart", status)
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(
            @RequestParam("memberId") String memberId) {
        log.info("Get cart for memberId={}", memberId);
        CartDTO cart = cartService.getCart(memberId);
        return ResponseEntity.ok(
                ApiResponse.success("Cart fetched successfully", cart)
        );
    }


    @DeleteMapping
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> clearCart(
            @RequestParam("memberId") String memberId) {
        log.info("Clear cart for memberId={}", memberId);
        cartService.clearCart(memberId);
        ErrorResponseDTO status = new ErrorResponseDTO("Cart cleared");
        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared", status)
        );
    }
}
