package com.example.api_gateway.client;

import com.example.api_gateway.request.AddToCartRequest;
import com.example.api_gateway.response.AddToCartResponse;
import com.example.api_gateway.response.CartItemListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "cart", url = "${services.cart.url}")
public interface CartServiceClient {

    @PostMapping("/cart/addProductToBag")
    ResponseEntity<AddToCartResponse> addProductToBag(@RequestBody AddToCartRequest request);

    @GetMapping("/cart/getAllProductInCart")
    ResponseEntity<CartItemListResponse> getAllCartProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam("customerId") UUID customerId);

    @DeleteMapping("/cart/deleteAllProductsByCustomerId")
    void deleteAllProductsByCustomerId(@RequestParam("customerId") UUID customerId);

    @DeleteMapping("/cart/deleteAllProductsByCustomerIdAndProductId")
    void deleteAllProductsByCustomerIdAndProductId(
            @RequestParam("customerId") UUID customerId,
            @RequestParam("productId") String productId);
}

