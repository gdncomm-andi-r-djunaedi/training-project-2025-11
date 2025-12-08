package com.blibi.cart.exception;

/**
 * Custom exception thrown when a cart is not found in the database.
 * 
 * This exception is used when operations are attempted on a cart that doesn't exist,
 * such as viewing or modifying a cart for a user who hasn't created one yet.
 * 
 * Exception Handling:
 * - Caught by GlobalExceptionHandler
 * - Returns HTTP 404 NOT FOUND status
 * - Provides user-friendly error message
 * 
 * Usage:
 * Thrown in CartServiceImpl when:
 * - getCart() is called for non-existent cart
 * - removeItem() is called for non-existent cart
 * 
 * @author Cart Service Team
 * @version 1.0
 */
public class CartNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new CartNotFoundException with the specified error message.
     * 
     * @param cartNotFound Error message describing why the cart was not found
     */
    public CartNotFoundException(String cartNotFound) {
        super(cartNotFound);
    }
}
