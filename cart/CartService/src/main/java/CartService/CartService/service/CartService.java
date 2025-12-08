package CartService.CartService.service;

import CartService.CartService.dto.CartRequestDto;
import CartService.CartService.dto.CartResponseDto;

public interface CartService {

        CartResponseDto addToCart(String userId, CartRequestDto request);

        CartResponseDto viewCart(String userId);

        CartResponseDto removeItem(String userId, String productId);

        CartResponseDto clearCart(String userId);

}
