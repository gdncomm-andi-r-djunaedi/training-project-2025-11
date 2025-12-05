package com.zasura.cart.controller;

import com.zasura.cart.dto.AddProductRequest;
import com.zasura.cart.dto.CommonResponse;
import com.zasura.cart.service.CartProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts/product")
public class CartProductController {
  @Autowired
  private CartProductService cartProductService;

  @PostMapping("/_add")
  public ResponseEntity<CommonResponse> addProduct(@RequestParam String userId,
      @Valid @RequestBody AddProductRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(CommonResponse.builder()
            .status(HttpStatus.OK.name())
            .code(HttpStatus.OK.value())
            .success(true)
            .data(cartProductService.addProduct(userId, request))
            .build());
  }

  @DeleteMapping("/_remove")
  public ResponseEntity<CommonResponse> removeProduct(@RequestParam String userId,
      @RequestParam String productId) {
    cartProductService.deleteProduct(userId, productId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(CommonResponse.builder()
            .status(HttpStatus.OK.name())
            .code(HttpStatus.OK.value())
            .success(true)
            .build());
  }
}
