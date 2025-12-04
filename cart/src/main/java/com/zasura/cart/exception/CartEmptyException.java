package com.zasura.cart.exception;

public class CartEmptyException extends RuntimeException {
  public CartEmptyException(String message) {
    super(message);
  }
}
