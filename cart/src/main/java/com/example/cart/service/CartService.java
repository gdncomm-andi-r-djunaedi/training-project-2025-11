package com.example.cart.service;

import com.example.cart.dto.CartDTO;
import com.example.cart.dto.ProductDTO;
import com.example.cart.dto.response.ProductServiceResponse;
import org.bson.types.ObjectId;

public interface CartService {
    CartDTO addProductToCart(ObjectId cartId, String productId);
    CartDTO getCart(ObjectId cartId);
    CartDTO deleteProductFromCart(ObjectId cartId, String productId);
}

