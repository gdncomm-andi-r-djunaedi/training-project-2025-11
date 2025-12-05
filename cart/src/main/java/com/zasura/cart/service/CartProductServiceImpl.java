package com.zasura.cart.service;

import com.zasura.cart.dto.AddProductRequest;
import com.zasura.cart.dto.AddProductResponse;
import com.zasura.cart.entity.Cart;
import com.zasura.cart.entity.CartProduct;
import com.zasura.cart.exception.CartEmptyException;
import com.zasura.cart.exception.ProductNotFoundException;
import com.zasura.cart.repository.CartProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartProductServiceImpl implements CartProductService {
  @Autowired
  CartProductRepository cartProductRepository;
  @Autowired
  CartService cartService;

  @Override
  public AddProductResponse addProduct(String userId, AddProductRequest addProductRequest) {
    Cart cart = cartService.getCart(userId);
    if (cart == null) {
      cart = cartService.createCart(userId);
    }
    CartProduct cartProduct =
        cartProductRepository.findByProductIdAndCartId(addProductRequest.getProductId(),
            cart.getId());
    if (cartProduct == null) {
      cartProduct = CartProduct.builder()
          .cart(cart)
          .productId(addProductRequest.getProductId())
          .quantity(addProductRequest.getQuantity())
          .build();
    } else {
      cartProduct.setQuantity(cartProduct.getQuantity() + addProductRequest.getQuantity());
    }
    cartProduct = cartProductRepository.save(cartProduct);
    return AddProductResponse.builder()
        .productId(cartProduct.getProductId())
        .quantity(cartProduct.getQuantity())
        .cartId(cartProduct.getCart().getId())
        .build();
  }

  @Override
  public void deleteProduct(String userId, String productId) {
    Cart cart = cartService.getCart(userId);
    if (cart == null) {
      throw new CartEmptyException("Cart is empty for user ID " + userId);
    }
    CartProduct cartProduct =
        cartProductRepository.findByProductIdAndCartId(productId, cart.getId());
    if (cartProduct == null) {
      throw new ProductNotFoundException("Product with ID " + productId + " not found in cart");
    }
    cartProductRepository.delete(cartProduct);
  }
}
