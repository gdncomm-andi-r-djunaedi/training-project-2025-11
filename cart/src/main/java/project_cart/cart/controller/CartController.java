package project_cart.cart.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project_cart.cart.entity.Cart;
import project_cart.cart.entity.CartItem;
import project_cart.cart.helper.HeaderHelper;
import project_cart.cart.model.CartItemResponse;
import project_cart.cart.model.CartRequest;
import project_cart.cart.model.CartResponse;
import project_cart.cart.model.CartItemRequest;
import project_cart.cart.service.CartService;

import java.util.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/findAllCart")
    public List<Cart> getCarts() {
        return cartService.getCarts();
    }

    //done need to improve
    @GetMapping("/viewCart")
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        String userId = HeaderHelper.getUserName(request).orElseThrow(() -> new RuntimeException("Header UserId empty"));
        Cart cart = cartService.getCartById(userId).orElseThrow(() -> new RuntimeException("Cart Not Found"));
        List<CartItem> cartItems = cartService.getCartItemById(cart.getId());
        List<CartItemResponse> cartItemResponses = cartItems.stream()
                .map(cartItem ->
                        CartItemResponse.builder()
                                .productId(cartItem.getProductId())
                                .quantity(cartItem.getQuantity())
                                .build()
                ).toList();
        return new ResponseEntity<>(CartResponse.builder()
                .userId(cart.getUserId())
                .cartItems(cartItemResponses)
                .build(), HttpStatus.OK);
    }

    @PostMapping("/addToCart")
    public Cart addToCart(@RequestBody CartRequest cartRequest, HttpServletRequest request) {
        String userId = HeaderHelper.getUserName(request).orElseThrow(() -> new RuntimeException("Header UserId empty"));
        cartRequest.setUserId(userId);
        return cartService.addToCart(cartRequest);
    }

    @DeleteMapping("/deleteProductCart")
    public ResponseEntity<Boolean> deleteCartItem(@RequestBody CartItemRequest cartItemRequest,  HttpServletRequest request) {
        String userId = HeaderHelper.getUserName(request).orElseThrow(() -> new RuntimeException("Header UserId empty"));
        cartItemRequest.setUserId(userId);
        cartService.deleteCartItem(cartItemRequest);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PutMapping("/updateCartItem")
    public ResponseEntity<Boolean> updateCartItem(@RequestBody CartRequest cartRequest, HttpServletRequest request) {
        String userId = HeaderHelper.getUserName(request).orElseThrow(() -> new RuntimeException("Header UserId empty"));
        cartRequest.setUserId(userId);
        cartService.updateCartItem(cartRequest);
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
