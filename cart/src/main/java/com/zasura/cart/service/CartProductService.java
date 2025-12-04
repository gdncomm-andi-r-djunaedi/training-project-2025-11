package com.zasura.cart.service;

import com.zasura.cart.dto.AddProductRequest;
import com.zasura.cart.dto.AddProductResponse;

public interface CartProductService {
  AddProductResponse addProduct(String userId, AddProductRequest addProductRequest);

  void deleteProduct(String userId, String productId);
}
