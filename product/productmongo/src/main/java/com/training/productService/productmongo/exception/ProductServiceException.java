package com.training.productService.productmongo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class ProductServiceException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public ProductServiceException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public static ProductServiceException notFound(String message) {
        return new ProductServiceException(message, HttpStatus.BAD_REQUEST, "400");
    }

    public static ProductServiceException badRequest(String message) {
        return new ProductServiceException(message, HttpStatus.BAD_REQUEST, "400");
    }

    public static ProductServiceException internalError(String message) {
        return new ProductServiceException(message, HttpStatus.INTERNAL_SERVER_ERROR, "500");
    }

    public static Exception invalidPayload() {
        return new ProductServiceException("Invalid payload", HttpStatus.BAD_REQUEST, "INVALID_PAYLOAD");
    }

    public static ProductServiceException duplicateSku(String sku) {
        return new ProductServiceException("Product with SKU '" + sku + "' already exists", HttpStatus.CONFLICT, "409");
    }
}
