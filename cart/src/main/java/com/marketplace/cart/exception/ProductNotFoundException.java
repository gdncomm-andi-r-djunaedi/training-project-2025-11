package com.marketplace.cart.exception;

import com.marketplace.common.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a product is not found in the product service.
 */
public class ProductNotFoundException extends BaseException {

    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId,
                HttpStatus.NOT_FOUND.value(),
                "PRODUCT_NOT_FOUND");
    }
}
