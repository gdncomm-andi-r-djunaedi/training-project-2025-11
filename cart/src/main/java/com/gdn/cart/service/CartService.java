package com.gdn.cart.service;

import com.gdn.cart.dto.request.AddCartItemRequestDTO;
import com.gdn.cart.dto.request.CartDTO;

public interface CartService {
    CartDTO addItem(String memberId, AddCartItemRequestDTO request);

    CartDTO getCart(String memberId);

    void deleteItem(String memberId, String itemId);

    void clearCart(String memberId);
}
