package org.edmund.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.edmund.cart.dto.AddToCartDto;
import org.edmund.cart.response.CartResponse;
import org.edmund.cart.services.CartService;
import org.edmund.commonlibrary.response.GenericResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "Add To Cart")
    public GenericResponse<CartResponse> addItemToCart(@RequestHeader(name = "X-User-Id") Long userId, @RequestBody AddToCartDto request) {
        try {
            cartService.addItem(userId, request.getProductSku(), request.getQuantity());
            CartResponse cart = cartService.getCart(userId);
            return GenericResponse.ok(cart);
        } catch (IllegalStateException e) {
            return GenericResponse.notFound(e.getMessage());
        } catch (Exception e) {
            return GenericResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<CartResponse> get(
            @RequestHeader(name = "X-User-Id") Long userId
    ) {
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }
}