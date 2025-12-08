package com.sc.cartservice.controller;

import com.sc.cartservice.dto.CartResponseDto;
import com.sc.cartservice.service.CartService;
import com.sc.utilsservice.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/create")
    private ResponseEntity<ApiResponse<String>> createCart(@RequestParam String memberId) {
        ApiResponse<String> response = cartService.createCart(memberId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponseDto>> addToCart(@RequestParam String memberId,
                                                                  @RequestParam String productCode) {
        ApiResponse<CartResponseDto> response = cartService.addToCart(memberId, productCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(@RequestParam String memberId) {
        ApiResponse<CartResponseDto> response = cartService.getCart(memberId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<CartResponseDto>> removeFromCart(@RequestParam String memberId,
                                                                       @RequestParam String productCode) {
        ApiResponse<CartResponseDto> response = cartService.removeFromCart(memberId, productCode);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{productCode}")
    public ResponseEntity<ApiResponse<CartResponseDto>> updateCartItemQuantity(@RequestParam String memberId,
                                                                               @PathVariable String productCode,
                                                                               @RequestParam int deltaQuantity) {
        ApiResponse<CartResponseDto> response = cartService.updateCartItemQuantity(memberId, productCode, deltaQuantity);
        return ResponseEntity.ok(response);
    }
}
