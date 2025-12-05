package com.blibli.cartmodule.controller;

import com.blibli.cartmodule.dto.Cartdto;
import com.blibli.cartmodule.dto.CartResponseDto;
import com.blibli.cartmodule.dto.ViewCartResponseDto;
import com.blibli.cartmodule.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    @Autowired
    CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartResponseDto> addToCart(@RequestBody Cartdto dto,
                                                      @RequestHeader("Authorization") String token) {
        log.info("Received add to cart request: productId={}, quantity={}", dto.getProductId(), dto.getQuantity());
        String memberId = com.blibli.cartmodule.util.TokenUtil.extractMemberIdFromToken(token);
        log.debug("Extracted memberId from token: {}", memberId);
        CartResponseDto response = cartService.addProductToCart(memberId, dto.getProductId(), dto.getQuantity());
        log.info("Add to cart completed. Status: {}, Product: {}, Quantity: {}", 
                response.getStatus(), response.getProductCode(), response.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearCart(@RequestHeader("Authorization") String token) {
        log.info("Received clear cart request");
        String memberId = com.blibli.cartmodule.util.TokenUtil.extractMemberIdFromToken(token);
        log.debug("Extracted memberId from token: {}", memberId);
        String message = cartService.clearCart(memberId);
        log.info("Clear cart completed for memberId: {}", memberId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/view")
    public ResponseEntity<ViewCartResponseDto> viewCart(@RequestHeader("Authorization") String token) {
        log.info("Received view cart request");
        ViewCartResponseDto cartResponseDto = cartService.viewCart(token);
        log.info("View cart completed. Status: {}, Total items: {}", 
                cartResponseDto.getStatus(), cartResponseDto.getTotalCartQuantity());
        return ResponseEntity.ok(cartResponseDto);
    }
}
