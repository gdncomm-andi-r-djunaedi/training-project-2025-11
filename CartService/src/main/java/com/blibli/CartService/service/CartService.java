package com.blibli.CartService.service;

import com.blibli.CartService.dto.AddToCartRequest;
import com.blibli.CartService.dto.CartResponseDto;

public interface CartService {
    CartResponseDto addOrUpdateCart(String userId, AddToCartRequest addToCartRequest);

    CartResponseDto viewCart(String userId);

    void deleteItem(String userId, String productId);
}
