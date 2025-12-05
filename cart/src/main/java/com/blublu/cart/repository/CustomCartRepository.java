package com.blublu.cart.repository;

import com.blublu.cart.document.CartDocument;
import com.blublu.cart.model.request.EditQtyRequest;

public interface CustomCartRepository {
  boolean addOrUpdateItem(String username, CartDocument.Item item);
  boolean editCartItem(String username, EditQtyRequest editQtyRequest);
  boolean removeItemFromCart(String username, String skuCode);
}
