package com.training.cartService.cartmongo.exception;

public class CartException extends RuntimeException {
    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class ProductNotFoundException extends CartException {
        public ProductNotFoundException(String sku) {
            super("Product not found with ID: " + sku);
        }
    }

    public static class InvalidRequestException extends CartException {
        public InvalidRequestException(String message) {
            super(message);
        }
    }

    public static class CartNotFoundException extends CartException {
        public CartNotFoundException(String userId) {
            super("Cart not found for user ID: " + userId);
        }
    }
}
