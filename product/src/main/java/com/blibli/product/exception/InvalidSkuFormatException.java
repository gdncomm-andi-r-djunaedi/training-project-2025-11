package com.blibli.product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSkuFormatException extends RuntimeException {
    public InvalidSkuFormatException(String sku) {
        super("Invalid SKU format: '" + sku + "'. Must match pattern: AAA-#####-#####.");
    }
}
