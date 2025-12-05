package com.zasura.cart.controller;

import com.zasura.cart.dto.CommonResponse;
import com.zasura.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
public class CartController {
  @Autowired
  private CartService cartService;

  @GetMapping("/_detail")
  public ResponseEntity<CommonResponse> getCartDetail(@RequestParam String userId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(CommonResponse.builder()
            .status(HttpStatus.OK.name())
            .code(HttpStatus.OK.value())
            .success(true)
            .data(cartService.getCart(userId))
            .build());
  }

  @DeleteMapping()
  public ResponseEntity<CommonResponse> removeCart(@RequestParam String userId) {
    cartService.deleteCart(userId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(CommonResponse.builder()
            .status(HttpStatus.OK.name())
            .code(HttpStatus.OK.value())
            .success(true)
            .build());
  }
}
