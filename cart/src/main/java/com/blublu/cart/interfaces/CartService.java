package com.blublu.cart.interfaces;

import com.blublu.cart.document.CartDocument;
import com.blublu.cart.model.request.EditQtyRequest;
import com.blublu.cart.model.response.CartResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface CartService {
  CartResponse getUserCart(String username);
  boolean addItemToCart(String username, CartDocument.Item item);
  boolean editCartItem(String username, EditQtyRequest editQtyRequest);
  boolean removeItemFromCart(String username, String skuCode);
  boolean clearCart(String username);
}
