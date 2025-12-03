package com.blibli.gdn.cartService.exception;

import org.springframework.http.HttpStatus;

public class SkuMismatchException extends CartException {
    public SkuMismatchException(String productId, String sku) {
        super("SKU does not belong to product. ProductId: " + productId + ", SKU: " + sku, HttpStatus.CONFLICT);
    }
}
