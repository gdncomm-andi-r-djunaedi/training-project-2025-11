package com.blibli.cartModule.service;

import com.blibli.cartModule.dto.AddItemRequestDto;
import com.blibli.cartModule.dto.CartResponseDto;
import com.blibli.cartModule.dto.RemoveItemDto;

public interface CartService {

    CartResponseDto addItem(Long memberId, AddItemRequestDto request);

    CartResponseDto removeItem(Long memberId, RemoveItemDto request);

    CartResponseDto getCart(Long memberId);

    void clearCart(Long memberId);
}
