package com.example.cart.exception;

import lombok.Getter;

@Getter
public class ProductUnavailableException extends RuntimeException {

  private final String productId;

  public ProductUnavailableException(String productId) {
    super("Product is not available: " + productId);
    this.productId = productId;
  }
}
