package com.example.cart.controller;

import com.example.cart.dto.CartResponse;
import com.example.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService service;

  @PostMapping("/{productId}")
  public ResponseEntity<CartResponse> add(
      @RequestHeader(name = "X-User-Id") Long userId,
      @PathVariable("productId") String productId,
      @RequestParam(name = "qty", defaultValue = "1") int qty
  ) {
    service.addItem(userId, productId, qty);
    CartResponse cart = service.getCart(userId);
    return ResponseEntity.ok(cart);
  }

  @GetMapping
  public ResponseEntity<CartResponse> get(
      @RequestHeader(name = "X-User-Id") Long userId
  ) {
    CartResponse cart = service.getCart(userId);
    return ResponseEntity.ok(cart);
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<CartResponse> remove(
      @RequestHeader(name = "X-User-Id") Long userId,
      @PathVariable("productId") String productId
  ) {
    service.removeItem(userId, productId);
    CartResponse cart = service.getCart(userId);
    return ResponseEntity.ok(cart);
  }

  @DeleteMapping
  public ResponseEntity<CartResponse> clear(
      @RequestHeader(name = "X-User-Id") Long userId
  ) {
    service.clear(userId);
    CartResponse cart = service.getCart(userId);
    return ResponseEntity.ok(cart);
  }
}