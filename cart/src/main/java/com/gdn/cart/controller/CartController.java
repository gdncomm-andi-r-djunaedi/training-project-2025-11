package com.gdn.cart.controller;

import com.gdn.cart.command.commandInterface.AddToCartCommand;
import com.gdn.cart.command.commandInterface.GetCartCommand;
import com.gdn.cart.command.commandInterface.RemoveFromCartCommand;
import com.gdn.cart.command.model.AddToCartCommandRequest;
import com.gdn.cart.command.model.GetCartCommandRequest;
import com.gdn.cart.command.model.RemoveFromCartCommandRequest;
import com.gdn.cart.controller.base.BaseCommandController;
import com.gdn.cart.controller.webmodel.request.AddToCartRequest;
import com.gdn.cart.controller.webmodel.response.CartResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController extends BaseCommandController {

  @GetMapping
  public ResponseEntity<CartResponse> getCart(
      @RequestHeader(value = "X-User-Id") String userId) {

    return ResponseEntity.ok(executor.execute(
        GetCartCommand.class,
        GetCartCommandRequest.builder()
            .memberId(userId)
            .build()));
  }

  @PostMapping
  public ResponseEntity<CartResponse> addToCart(
      @RequestHeader(value = "X-User-Id") String userId,
      @Valid @RequestBody AddToCartRequest request) {

    return ResponseEntity.ok(executor.execute(
        AddToCartCommand.class,
        AddToCartCommandRequest.builder()
            .memberId(userId)
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .build()));
  }

  @DeleteMapping("/items/{productId}")
  public ResponseEntity<CartResponse> removeFromCart(
      @RequestHeader(value = "X-User-Id") String userId,
      @PathVariable(value = "productId") String productId) {

    return ResponseEntity.ok(executor.execute(
        RemoveFromCartCommand.class,
        RemoveFromCartCommandRequest.builder()
            .memberId(userId)
            .productId(productId)
            .build()));
  }
}

