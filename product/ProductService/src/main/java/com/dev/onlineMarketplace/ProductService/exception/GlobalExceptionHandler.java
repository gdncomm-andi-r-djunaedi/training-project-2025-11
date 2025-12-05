package com.dev.onlineMarketplace.ProductService.exception;

import com.dev.onlineMarketplace.ProductService.dto.GdnResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GdnResponseData<String>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage());
        if (ex.getMessage() != null && ex.getMessage().contains("Product not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GdnResponseData.error("Product not found"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GdnResponseData.error("Internal server error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GdnResponseData<String>> handleException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GdnResponseData.error("Internal server error"));
    }
}
