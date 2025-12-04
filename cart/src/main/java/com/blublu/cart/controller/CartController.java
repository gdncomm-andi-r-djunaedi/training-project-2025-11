package com.blublu.cart.controller;

import com.blublu.cart.document.CartDocument;
import com.blublu.cart.interfaces.CartService;
import com.blublu.cart.model.request.EditQtyRequest;
import com.blublu.cart.model.response.CartResponse;
import com.blublu.cart.model.response.GenericBodyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@RestController
@RequestMapping("/api/cart")
public class CartController {

  @Autowired
  CartService cartService;

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getCartItems(@RequestParam String username) {
    CartResponse cart = cartService.getUserCart(username);

    if (Objects.isNull(cart)) {
      return ResponseEntity.internalServerError()
          .body(GenericBodyResponse.builder()
              .content(new ArrayList<>())
              .errorMessage("Cart not found")
              .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
              .success(false)
              .build());
    } else {
      return ResponseEntity.ok()
          .body(GenericBodyResponse.builder().content(Collections.singletonList(cart)).success(true).build());
    }
  }

  @RequestMapping(value = "/items", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> addItemToCart(@RequestParam String username, @RequestBody CartDocument.Item item) {
    return cartService.addItemToCart(username, item) ?
        ResponseEntity.ok().body(GenericBodyResponse.builder().success(true).build()) :
        ResponseEntity.internalServerError()
            .body(GenericBodyResponse.builder()
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage("SKU not found!")
                .success(false)
                .build());
  }

  @RequestMapping(value = "/items", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> deleteCart(@RequestParam String username) {
    return cartService.clearCart(username) ?
        ResponseEntity.ok().body(GenericBodyResponse.builder().success(true).build()) :
        ResponseEntity.internalServerError()
            .body(GenericBodyResponse.builder()
                .success(false)
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage("Cart not found!")
                .build());
  }

  @RequestMapping(value = "/item/edit", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> editCartItem(@RequestParam String username, @RequestBody EditQtyRequest item) {
    return cartService.editCartItem(username, item) ?
        ResponseEntity.ok().body(GenericBodyResponse.builder().success(true).build()) :
        ResponseEntity.internalServerError()
            .body(GenericBodyResponse.builder()
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage("User cart or item does not exist!")
                .success(false)
                .build());
  }

  @RequestMapping(value = "/item/{skuCode}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> deleteItemFromCart(@RequestParam String username, @PathVariable String skuCode) {
    return cartService.removeItemFromCart(username, skuCode) ?
        ResponseEntity.ok().body(GenericBodyResponse.builder().success(true).build()) :
        ResponseEntity.internalServerError()
            .body(GenericBodyResponse.builder()
                .errorMessage("Item not found in user cart!")
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .success(false)
                .build());
  }
}
