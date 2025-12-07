package com.blibli.cartData.services;


import com.blibli.cartData.dto.CartDTO;
import com.blibli.cartData.dto.CartItemDTO;
import com.blibli.cartData.dto.CartProductDetailDTO;
import com.blibli.cartData.dto.CartResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartService {
    Page<CartProductDetailDTO> getAllCartItems(String authToken, Pageable pageable);

    CartResponseDTO addProductToCart(String authToken, CartItemDTO cartItemDTO);

    void deleteCartItem(String authToken,String productId);
}
