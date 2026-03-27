package com.ecom.cart.Controller;


import com.ecom.cart.Dto.ApiResponse;
import com.ecom.cart.Dto.CartDto;
import com.ecom.cart.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping(value = "/getCart", produces = "application/json")
    public ApiResponse<CartDto> getCartByUserId(@RequestParam("userId") String userId){
        CartDto cart = cartService.getCartByUserId(userId);
        return ApiResponse.success(200, cart);
    }

    @DeleteMapping("/deleteCart")
    public ApiResponse<String> deleteCartByUserId(@RequestParam("sku") String sku, @RequestParam("userId") String userId){
        cartService.deleteFromCartBySku(sku, userId);
        return ApiResponse.success(200, "Success!! product deleted from cart");
    }

    @PostMapping("/addToCart")
    public ApiResponse<String> addToCartForUserId(@RequestParam("userId") String userId, @RequestParam("sku") String sku){
        cartService.addSkuToCart(sku, userId);
        return ApiResponse.success(200, "Success!! product added to cart");

    }

}
