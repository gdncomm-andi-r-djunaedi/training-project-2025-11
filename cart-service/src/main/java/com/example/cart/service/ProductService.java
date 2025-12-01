package com.example.cart.service;

public interface ProductService {

  /**
   * @return true if product exists in product-service
   */
  boolean exists(String productId);
}
