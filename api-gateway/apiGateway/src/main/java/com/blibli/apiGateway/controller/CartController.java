package com.blibli.apiGateway.controller;

import com.blibli.apiGateway.client.CartClient;
import com.blibli.apiGateway.dto.CartItemDTO;
import com.blibli.apiGateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartClient cartClient;

    private final JwtUtil jwtUtil;

    public CartController(CartClient cartClient, JwtUtil jwtUtil) {
        this.cartClient = cartClient;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/addItem")
    public ResponseEntity<?> addProductToCart(Authentication authentication,
                                              @RequestBody CartItemDTO cartItemDTO) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized", "message", "JWT token is missing or invalid", "timestamp", Instant.now().toString()
                        ));
            }
            String email = authentication.getName();
            String token = jwtUtil.generateToken(email);
            return cartClient.addProductToCart("Bearer " + token, cartItemDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", e.getMessage(), "timestamp", Instant.now().toString()
                    ));
        }
    }


    @DeleteMapping("/deleteItem/{productId}")
    public ResponseEntity<?> deleteProductFromCart(Authentication authentication,
                                                   @PathVariable String productId) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized",
                                "message", "JWT token is missing or invalid",
                                "timestamp", Instant.now().toString()));
            }
            String email = authentication.getName();

            String token = jwtUtil.generateToken(email);
            return cartClient.deleteProductFromCart("Bearer " + token, productId);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete item",
                            "message", ex.getMessage(),
                            "timestamp", Instant.now().toString()));
        }
    }


    @GetMapping("/viewItem")
    public ResponseEntity<?> getAllCartProducts(Authentication authentication,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "5") int size) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized",
                                "message", "JWT token is missing or invalid",
                                "timestamp", Instant.now().toString()));
            }

            String email = authentication.getName();
            String token = jwtUtil.generateToken(email);

            return cartClient.viewCartItems("Bearer " + token, page, size);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch cart items",
                            "message", ex.getMessage(),
                            "timestamp", Instant.now().toString()));
        }
    }
}
