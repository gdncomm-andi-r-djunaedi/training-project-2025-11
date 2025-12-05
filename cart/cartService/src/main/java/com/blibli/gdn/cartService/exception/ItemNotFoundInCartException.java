package com.blibli.gdn.cartService.exception;

import org.springframework.http.HttpStatus;

public class ItemNotFoundInCartException extends CartException {
    public ItemNotFoundInCartException(String sku) {
        super("Item not found in cart for SKU: " + sku, HttpStatus.NOT_FOUND);
    }
}
