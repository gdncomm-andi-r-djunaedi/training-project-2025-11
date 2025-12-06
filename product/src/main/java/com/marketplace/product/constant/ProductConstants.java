package com.marketplace.product.constant;

/**
 * Constants for Product Service
 */
public final class ProductConstants {

    private ProductConstants() {
        // Prevent instantiation
    }

    /**
     * Error messages
     */
    public static final class ErrorMessages {
        public static final String PRODUCT_NOT_FOUND = "Product not found with ID: %s";
        public static final String INVALID_PRICE = "Price must be greater than 0";
        public static final String INVALID_STOCK = "Stock cannot be negative";

        private ErrorMessages() {
        }
    }

    /**
     * Validation messages
     */
    public static final class ValidationMessages {
        public static final String NAME_REQUIRED = "Product name is required";
        public static final String DESCRIPTION_REQUIRED = "Description is required";
        public static final String PRICE_REQUIRED = "Price is required";
        public static final String PRICE_MIN = "Price must be greater than 0";
        public static final String CATEGORY_REQUIRED = "Category is required";
        public static final String STOCK_REQUIRED = "Stock is required";
        public static final String STOCK_MIN = "Stock cannot be negative";

        private ValidationMessages() {
        }
    }

    /**
     * Product categories
     */
    public static final class Categories {
        public static final String ELECTRONICS = "Electronics";
        public static final String CLOTHING = "Clothing";
        public static final String BOOKS = "Books";
        public static final String HOME = "Home & Kitchen";
        public static final String SPORTS = "Sports";

        private Categories() {
        }
    }
}
