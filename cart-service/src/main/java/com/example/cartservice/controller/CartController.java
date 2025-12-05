package com.example.cartservice.controller;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.AddToCartResponse;
import com.example.cartservice.dto.GetCartResponse;
import com.example.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/addToCart")
    public ResponseEntity<AddToCartResponse> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddToCartRequest request) {

        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }

    @GetMapping("/getCart")
    public ResponseEntity<Page<GetCartResponse>> getCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(cartService.getCart(
                userId,
                PageRequest.of(page, size)
        ));
    }

    @DeleteMapping("/deleteFromCart/{id}")
    public ResponseEntity<Boolean> deleteFromCart(@PathVariable String id) {
        return ResponseEntity.ok(cartService.deleteFromCart(id));
    }
}