package com.training.marketplace.gateway.controller;


import com.training.marketplace.cart.modal.request.AddProductToCartRequest;
import com.training.marketplace.cart.modal.request.RemoveProductFromCartRequest;
import com.training.marketplace.cart.modal.request.ViewCartRequest;
import com.training.marketplace.cart.modal.response.DefaultCartResponse;
import com.training.marketplace.cart.modal.response.ViewCartResponse;
import com.training.marketplace.gateway.client.CartClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartClientImpl cartClient;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/addToCart")
    public DefaultCartResponse postAddProductToCart(@RequestBody AddProductToCartRequest request){
        return cartClient.addProductToCart(request);
    }

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ViewCartResponse getViewCart(@RequestBody ViewCartRequest request){
        return cartClient.viewCart(request);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public DefaultCartResponse removeProductFromCart(@RequestBody RemoveProductFromCartRequest request){
        return cartClient.removeProductFromCart(request);
    }
}
