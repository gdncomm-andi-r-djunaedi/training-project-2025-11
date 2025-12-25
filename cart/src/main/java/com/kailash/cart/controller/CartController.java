package com.kailash.cart.controller;

import com.kailash.cart.dto.AddItemRequest;
import com.kailash.cart.dto.ApiResponse;
import com.kailash.cart.dto.CartResponse;
import com.kailash.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService svc;

    @Autowired
    public CartController(CartService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ApiResponse<CartResponse> get(@RequestHeader(name = "X-User-Id") String memberId) {
        return svc.getCart(memberId);
    }

    @PostMapping
    public ApiResponse<CartResponse> add(
            @RequestHeader(name = "X-User-Id") String memberId,
            @RequestBody AddItemRequest req) {
        return svc.addOrUpdateItem(memberId, req.getSku(), req.getQty());
    }

    @DeleteMapping("/items/{sku}")
    public ApiResponse<CartResponse> remove(
            @RequestHeader(name = "X-User-Id") String memberId,
            @PathVariable("sku") String sku) {
        return svc.removeItem(memberId, sku);
    }
}
