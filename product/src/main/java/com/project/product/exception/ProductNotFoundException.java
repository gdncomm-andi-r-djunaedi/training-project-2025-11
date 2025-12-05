package com.project.product.exception;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(String message){
        super(message);
    }

    public ProductNotFoundException(String field, String value){
        super(String.format("Product not found with %s: %s", field, value));
    }
}
