package org.jovian.product.exception;

public class ProductDoesntExistException extends RuntimeException {
    public ProductDoesntExistException() {
        super("Sorry product doesn't exist");
    }
}
