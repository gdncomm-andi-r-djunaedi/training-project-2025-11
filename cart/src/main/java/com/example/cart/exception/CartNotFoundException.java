package com.example.cart.exception;

import java.io.Serial;
import java.io.Serializable;

public class CartNotFoundException
        extends RuntimeException
        implements Serializable {
    @Serial
    public static final long serialVersionUID = 4328743;
    public CartNotFoundException(String message) {
        super(message);
    }
}

