package com.example.cart.controller;

import com.example.cart.dto.AddToCartRequest;
import com.example.cart.dto.AddToCartResponse;
import com.example.cart.dto.CartItemListResponse;
import com.example.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/addProductToBag")
    public ResponseEntity<AddToCartResponse> addProductToBag(@RequestBody AddToCartRequest addToCartRequest){
        AddToCartResponse addToCartResponse = cartService.addProductToCart(addToCartRequest);
        return new ResponseEntity<>(addToCartResponse, HttpStatus.OK);
    }

    @GetMapping("/getAllProductInCart")
    public ResponseEntity<CartItemListResponse> getAllCartProducts(@RequestParam(name = "page", defaultValue = "0")int page
            , @RequestParam(name = "size",defaultValue = "5")int size, UUID customerId){
        CartItemListResponse addToCartResponses = cartService.getAllCartProducts(page,size,customerId);
        return new ResponseEntity<>(addToCartResponses, HttpStatus.OK);
    }

    @DeleteMapping("/deleteAllProductsByCustomerId")
    public void deleteAllProductsByCustomerId(@RequestParam("customerId")UUID customerId){
        cartService.deleteAllCartItemsByCustomerId(customerId);
    }

    @DeleteMapping("/deleteAllProductsByCustomerIdAndProductId")
    public void deleteAllProductsByCustomerIdAndProductId(@RequestParam("customerId")UUID customerId,@RequestParam("productId")String productId){
        cartService.deleteAllCartItemsByCustomerIdProductId(customerId,productId);
    }
}
