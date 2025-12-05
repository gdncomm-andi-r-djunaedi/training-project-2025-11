package com.blibli.cart.service;

import com.blibli.cart.dto.AddToCartRequestDTO;
import com.blibli.cart.dto.AddToCartResponseDTO;

public interface CartService {
    AddToCartResponseDTO addProductToCart(String customerEmail, AddToCartRequestDTO addToCartRequestDTO);

    AddToCartResponseDTO viewCart(String customerEmail);

    AddToCartResponseDTO deleteBySku(String customerEmail,String productSku);

    AddToCartResponseDTO deletAllItems(String customerEmail);
}
