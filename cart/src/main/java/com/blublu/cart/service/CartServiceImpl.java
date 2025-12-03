package com.blublu.cart.service;

import com.blublu.cart.document.CartDocument;
import com.blublu.cart.interfaces.CartService;
import com.blublu.cart.interfaces.ProductFeignClient;
import com.blublu.cart.model.request.EditQtyRequest;
import com.blublu.cart.model.response.CartResponse;
import com.blublu.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

  @Autowired
  CartRepository cartRepository;

  @Autowired
  ProductFeignClient productFeignClient;

  @Override
  public CartResponse getUserCart(String username) {
    CartDocument cartDocument = cartRepository.findByUsername(username);

    if (Objects.isNull(cartDocument)) {
      return null;
    }

    CartResponse cartResponse = CartResponse.builder().username(cartDocument.getUsername()).items(new ArrayList<>()).build();
    for (CartDocument.Item item : cartDocument.getItems()) {
      CartResponse.ItemResponse itemResponse = productFeignClient.getProductDetail(item.getSkuCode()).getContent().getFirst();
      itemResponse.setQuantity(item.getQuantity());
      cartResponse.getItems().add(itemResponse);
    }

    return cartResponse;
  }

  @Override
  public boolean addItemToCart(String username, CartDocument.Item item) {
    return cartRepository.addOrUpdateItem(username, item);
  }

  @Override
  public boolean editCartItem(String username, EditQtyRequest editQtyRequest) {
    return cartRepository.editCartItem(username, editQtyRequest);
  }

  @Override
  public boolean removeItemFromCart(String username, String skuCode) {
    return cartRepository.removeItemFromCart(username, skuCode);
  }

  @Override
  public boolean clearCart(String username) {
    return cartRepository.deleteTopByUsername(username) > 0;
  }
}
