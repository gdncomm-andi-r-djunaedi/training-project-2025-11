package com.training.cartService.cartmongo.service;

import com.training.cartService.cartmongo.dto.AddToCartRequest;
import com.training.cartService.cartmongo.dto.CartResponse;

import javax.validation.Valid;

public interface CartService {
    CartResponse addToCart(String userId, @Valid AddToCartRequest request);

    CartResponse getCart(String userId);

    CartResponse deleteCartItems(String userId);
}
