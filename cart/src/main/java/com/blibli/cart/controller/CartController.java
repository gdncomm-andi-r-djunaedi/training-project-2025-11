package com.blibli.cart.controller;

import com.blibli.cart.dto.AddToCartRequestDTO;
import com.blibli.cart.dto.AddToCartResponseDTO;
import com.blibli.cart.response.GdnResponse;
import com.blibli.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    CartService cartService;
    @PostMapping("/cart/addProductToCart")
    public ResponseEntity<GdnResponse<AddToCartResponseDTO>> addProductToCart(@RequestParam String customerEmail, @RequestBody AddToCartRequestDTO addToCartRequestDTO){
        return new ResponseEntity<>(new GdnResponse(true,null,cartService.addProductToCart(customerEmail,addToCartRequestDTO)), HttpStatus.OK);
    }

    @GetMapping("/cart/viewCart")
    public ResponseEntity<GdnResponse<AddToCartResponseDTO>> viewCart(@RequestParam String customerEmail){
        return new ResponseEntity<>(new GdnResponse(true,null,cartService.viewCart(customerEmail)),HttpStatus.OK);
    }

    @DeleteMapping("/cart/deletBySku")
    public ResponseEntity<GdnResponse<AddToCartResponseDTO>> deletBySku(@RequestParam String customerEmail,@RequestParam String productSku){
        return new ResponseEntity<>(new GdnResponse(true,null,cartService.deleteBySku(customerEmail,productSku)),HttpStatus.OK);
    }

    @DeleteMapping("/cart/deleteAllItems")
    public ResponseEntity<GdnResponse<AddToCartResponseDTO>> deleteAllItems(@RequestParam String customerEmail){
        return new ResponseEntity<>(new GdnResponse(true,null,cartService.deletAllItems(customerEmail)),HttpStatus.OK);
    }



}
