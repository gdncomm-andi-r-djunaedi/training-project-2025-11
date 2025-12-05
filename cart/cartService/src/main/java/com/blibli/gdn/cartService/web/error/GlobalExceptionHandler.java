package com.blibli.gdn.cartService.web.error;

import com.blibli.gdn.cartService.exception.CartException;
import com.blibli.gdn.cartService.exception.CartNotFoundException;
import com.blibli.gdn.cartService.exception.InvalidQuantityException;
import com.blibli.gdn.cartService.exception.ItemNotFoundInCartException;
import com.blibli.gdn.cartService.exception.ProductNotFoundException;
import com.blibli.gdn.cartService.exception.ProductServiceUnavailableException;
import com.blibli.gdn.cartService.exception.SkuMismatchException;
import com.blibli.gdn.cartService.web.model.GdnResponseData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<GdnResponseData<Object>> handleProductNotFoundException(
            ProductNotFoundException ex, HttpServletRequest request) {

        log.error("Product not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<GdnResponseData<Object>> handleCartNotFoundException(
            CartNotFoundException ex, HttpServletRequest request) {

        log.error("Cart not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ItemNotFoundInCartException.class)
    public ResponseEntity<GdnResponseData<Object>> handleItemNotFoundInCartException(
            ItemNotFoundInCartException ex, HttpServletRequest request) {

        log.error("Item not found in cart: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<GdnResponseData<Object>> handleInvalidQuantityException(
            InvalidQuantityException ex, HttpServletRequest request) {

        log.error("Invalid quantity: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(SkuMismatchException.class)
    public ResponseEntity<GdnResponseData<Object>> handleSkuMismatchException(
            SkuMismatchException ex, HttpServletRequest request) {

        log.error("SKU mismatch: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ProductServiceUnavailableException.class)
    public ResponseEntity<GdnResponseData<Object>> handleProductServiceUnavailableException(
            ProductServiceUnavailableException ex, HttpServletRequest request) {

        log.error("Product Service unavailable: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GdnResponseData<Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.error("Validation error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @ExceptionHandler(CartException.class)
    public ResponseEntity<GdnResponseData<Object>> handleCartException(
            CartException ex, HttpServletRequest request) {

        log.error("Cart exception: {}", ex.getMessage());
        return buildErrorResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GdnResponseData<Object>> handleException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<GdnResponseData<Object>> buildErrorResponse(HttpStatus status, String message) {
        String traceId = UUID.randomUUID().toString();

        GdnResponseData<Object> response = GdnResponseData.builder()
                .success(false)
                .message(message)
                .data(null)
                .traceId(traceId)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
