package com.example.cart.web;

import com.example.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService service;

  @PostMapping("/{productId}")
  public void add(@RequestHeader(name = "X-User-Id") Long userId,
      @PathVariable("productId") String productId,
      @RequestParam(name = "qty", defaultValue = "1") int qty) {
    service.addItem(userId, productId, qty);
  }

  @GetMapping
  public Map<Object, Object> get(@RequestHeader(name = "X-User-Id") Long userId) {
    return service.getCart(userId);
  }

  @DeleteMapping("/{productId}")
  public void remove(@RequestHeader(name = "X-User-Id") Long userId,
      @PathVariable("productId") String productId) {
    service.removeItem(userId, productId);
  }

  @DeleteMapping
  public void clear(@RequestHeader(name = "X-User-Id") Long userId) {
    service.clear(userId);
  }
}