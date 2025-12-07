package com.Product.ProductService.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ProductServiceExceptions extends RuntimeException {
    public ProductServiceExceptions(String message, HttpStatus status) {
        super(message);
    }
}
