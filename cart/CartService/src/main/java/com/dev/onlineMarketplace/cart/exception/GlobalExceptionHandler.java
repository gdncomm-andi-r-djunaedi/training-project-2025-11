package com.dev.onlineMarketplace.cart.exception;

import com.dev.onlineMarketplace.cart.dto.GdnResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<GdnResponseData<String>> handleProductNotFoundException(ProductNotFoundException e) {
        return new ResponseEntity<>(GdnResponseData.error(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GdnResponseData<String>> handleException(Exception e) {
        return new ResponseEntity<>(GdnResponseData.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GdnResponseData<String>> handleRuntimeException(RuntimeException e) {
        return new ResponseEntity<>(GdnResponseData.error(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
