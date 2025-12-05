package com.example.cart.service;

import com.example.cart.dto.AddToCartRequest;
import com.example.cart.dto.AddToCartResponse;
import com.example.cart.dto.CartItemListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CartService {
    AddToCartResponse addProductToCart(AddToCartRequest addToCart);
    CartItemListResponse getAllCartProducts( int pageNumber, int pageSize, UUID customerId);
    void deleteAllCartItemsByCustomerId(UUID customerId);
    void deleteAllCartItemsByCustomerIdProductId(UUID customerId,String productId);
}
