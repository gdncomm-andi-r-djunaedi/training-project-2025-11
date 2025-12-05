package org.jovian.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jovian.cart.entity.CartItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jovian.cart.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/createNewCart")
    @Operation(summary="Get or Create New Cart")
    public ResponseEntity<String> createCart(@PathVariable Long cartId) {
        cartService.getOrCreateCart(cartId);
        return ResponseEntity.ok("Cart created with ID: " + cartId);
    }

    @PostMapping("/addProductToCart")
    @Operation(summary="Add Product To Cart")
    public ResponseEntity<String> addProduct(
            @PathVariable Long cartId,
            @PathVariable String productId,
            @PathVariable int quantity) {
        cartService.addProduct(cartId, productId, quantity);
        return ResponseEntity.ok("Product added to cart");
    }

    @DeleteMapping("/deleteProductFromCart")
    @Operation(summary="Delete Product in Cart")
    public ResponseEntity<String> removeProduct(
            @PathVariable Long cartId,
            @PathVariable String productId,
            @PathVariable int quantity) {
        cartService.removeProduct(cartId, productId, quantity);
        return ResponseEntity.ok("Product removed from cart");
    }

    @GetMapping("/getCartContent")
    @Operation(summary="Get Cart Items")
    public ResponseEntity<List<CartItem>> getCartProducts(@PathVariable Long cartId) {
        List<CartItem> products = cartService.getCartProducts(cartId);
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/clearCart")
    @Operation(summary="Clear All Products in Cart")
    public ResponseEntity<String> clearCart(
            @PathVariable Long cartId
    ) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok("Cart cleared");
    }
}
