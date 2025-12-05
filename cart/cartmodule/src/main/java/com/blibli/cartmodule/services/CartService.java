package com.blibli.cartmodule.services;

import com.blibli.cartmodule.dto.CartResponseDto;
import com.blibli.cartmodule.dto.ViewCartResponseDto;

public interface CartService {

    CartResponseDto addProductToCart(String memberId, String productCode, int quantity);

    String clearCart(String memberId);

    ViewCartResponseDto viewCart(String token);

    void validateMemberId(String memberId);
}
