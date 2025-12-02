package com.example.cart.service;

import com.example.cart.dto.CartDTO;
import com.example.cart.dto.ProductDTO;
import org.bson.types.ObjectId;

public interface CartService {
    CartDTO addProductToCart(ObjectId cartId, ProductDTO productDTO);
    CartDTO getCart(ObjectId cartId);
    CartDTO deleteProductFromCart(ObjectId cartId, String productId);
}

