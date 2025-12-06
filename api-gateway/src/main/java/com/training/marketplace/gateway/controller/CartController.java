package com.training.marketplace.gateway.controller;


import com.training.marketplace.cart.modal.request.AddProductToCartRequest;
import com.training.marketplace.cart.modal.request.RemoveProductFromCartRequest;
import com.training.marketplace.cart.modal.request.ViewCartRequest;
import com.training.marketplace.cart.modal.response.DefaultCartResponse;
import com.training.marketplace.cart.modal.response.ViewCartResponse;
import com.training.marketplace.gateway.dto.cart.AddProductToCartRequestDTO;
import com.training.marketplace.gateway.dto.cart.DefaultCartResponseDTO;
import com.training.marketplace.gateway.dto.cart.ProductCartDTO;
import com.training.marketplace.gateway.dto.cart.RemoveProductFromCartRequestDTO;
import com.training.marketplace.gateway.dto.cart.ViewCartRequestDTO;
import com.training.marketplace.gateway.dto.cart.ViewCartResponseDTO;
import com.training.marketplace.gateway.service.CartClientService;

import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartClientService cartClient;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/addToCart")
    public DefaultCartResponseDTO postAddProductToCart(@RequestBody AddProductToCartRequestDTO request) {
        log.info(String.format("adding to cart for %s, product %s, and qty %s", request.getUserId(), request.getProductId(), request.getQuantity()));
        DefaultCartResponse response = cartClient.addProductToCart(AddProductToCartRequest.newBuilder()
                .setUserId(request.getUserId())
                .setProductId(request.getProductId())
                .setQuantity(request.getQuantity())
                .build());
        return DefaultCartResponseDTO.builder()
                .success(response.getSuccess())
                .message(response.getMessage())
                .build();
    }

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ViewCartResponseDTO getViewCart(@RequestBody ViewCartRequestDTO request) {
        log.info(String.format("viewing cart for %s", request.getUserId()));
        ViewCartResponse response = cartClient.viewCart(ViewCartRequest.newBuilder()
                .setUserId(request.getUserId())
                .build());
        return ViewCartResponseDTO.builder()
                .userId(response.getUserId())
                .products(response.getProductsList().stream().map(product -> ProductCartDTO.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .productPrice(product.getProductPrice())
                        .productImage(product.getProductImage())
                        .productCartQuantity(product.getProductCartQuantity())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/removeFromCart")
    public DefaultCartResponseDTO removeProductFromCart(@RequestBody RemoveProductFromCartRequestDTO request) {
        log.info(String.format("removing product %s from cart for %s, qty %s", request.getProductId(), request.getUserId(), request.getQuantity()));
        DefaultCartResponse response = cartClient.removeProductFromCart(RemoveProductFromCartRequest.newBuilder()
                .setUserId(request.getUserId())
                .setProductId(request.getProductId())
                .setQuantity(request.getQuantity())
                .build());
        return DefaultCartResponseDTO.builder()
                .success(response.getSuccess())
                .message(response.getMessage())
                .build();
    }
}
