package com.project.product.exception;

public class DuplicateSkuException extends RuntimeException{
    public DuplicateSkuException(String sku) {
        super(String.format("Product with SKU '%s' already exists", sku));
    }
}
