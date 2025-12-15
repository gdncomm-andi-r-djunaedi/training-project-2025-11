package com.blibli.api_gateway.controller;


import com.blibli.api_gateway.dto.*;
import com.blibli.api_gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GatewayCartController {
    @Autowired
    GatewayService gatewayService;

    @PostMapping("/cart/addProductToCart")
    public ResponseEntity<AddToCartResponseDTO> addProductToCart(@RequestParam String token,@RequestBody AddToCartRequestDTO addToCartRequestDTO){
        return new ResponseEntity<>(gatewayService.addProductToCart(token,addToCartRequestDTO), HttpStatus.OK);
    }

    @GetMapping("/cart/viewCart")
    public ResponseEntity<AddToCartResponseDTO> viewCart(@RequestParam String token ){
        return new ResponseEntity<>(gatewayService.viewCart(token),HttpStatus.OK);
    }

    @DeleteMapping("/cart/deletBySku")
    public ResponseEntity<AddToCartResponseDTO> deletBySku(@RequestParam String token,@RequestParam String productSku){
        return new ResponseEntity<>(gatewayService.deleteBySku(token,productSku),HttpStatus.OK);
    }

    @DeleteMapping("/cart/deleteAllItems")
    public ResponseEntity<AddToCartResponseDTO> deleteAllItems(@RequestParam String token ){
        return new ResponseEntity<>(gatewayService.deletAllItems(token),HttpStatus.OK);
    }



}
