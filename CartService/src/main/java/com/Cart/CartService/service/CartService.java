package com.Cart.CartService.service;

import com.Cart.CartService.dto.AddItemRequestDTO;
import com.Cart.CartService.dto.CartResponseDTO;

public interface CartService {

    CartResponseDTO addItem(String memberId, AddItemRequestDTO request);

    boolean deleteProductFromCart(String userName, String itemId);

    CartResponseDTO getCartByMemberId(String memberId);
}
