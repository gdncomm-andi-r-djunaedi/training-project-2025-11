package com.example.api_gateway.service;

import com.example.api_gateway.request.AddToCart;
import com.example.api_gateway.request.AddToCartRequest;
import com.example.api_gateway.response.AddToCartResponse;
import com.example.api_gateway.response.CartItemListResponse;

import java.util.UUID;

public interface CartService {

    AddToCartResponse addProductToBag(String token, AddToCart addToCart) throws Exception;
    CartItemListResponse getAllCartProductsOFCustomer(String token, int page, int size) throws Exception;
    void deleteAllproductsByCustomerId(String token) throws Exception;
    void deleteAllProductsByCustomeridAndProductId(String token,String productId) throws Exception;
}
