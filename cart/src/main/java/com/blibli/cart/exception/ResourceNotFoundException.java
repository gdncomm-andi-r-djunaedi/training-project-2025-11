package com.blibli.cart.exception;

public class ResourceNotFoundException  extends RuntimeException{
    public ResourceNotFoundException(String messages) {
        super(messages);
    }
}
