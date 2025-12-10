package com.marketplace.cart.constant;

/**
 * Constants for Cart Service
 */
public final class CartConstants {

    private CartConstants() {
        // Prevent instantiation
    }

    /**
     * Error messages
     */
    public static final class ErrorMessages {
        public static final String CART_NOT_FOUND = "Cart not found for user: %s";
        public static final String CART_ITEM_NOT_FOUND = "Cart item not found for product ID: %s";
        public static final String INVALID_QUANTITY = "Quantity must be greater than 0";
        public static final String INVALID_PRICE = "Price must be greater than 0";

        private ErrorMessages() {
        }
    }

    /**
     * Validation messages
     */
    public static final class ValidationMessages {
        public static final String PRODUCT_ID_REQUIRED = "Product ID is required";
        public static final String PRODUCT_NAME_REQUIRED = "Product name is required";
        public static final String PRICE_REQUIRED = "Price is required";
        public static final String QUANTITY_REQUIRED = "Quantity is required";
        public static final String QUANTITY_MIN = "Quantity must be at least 1";
        public static final String PRICE_MIN = "Price must be greater than 0";

        private ValidationMessages() {
        }
    }
}
