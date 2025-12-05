package com.ecom.cart.Service;

import com.ecom.cart.Dto.CartDto;

import java.util.List;

public interface CartService {

    CartDto getCartByUserId(String userId);

    Boolean deleteFromCartBySku(String sku, String userId);

    Boolean addSkuToCart(String sku, String userId);

}
