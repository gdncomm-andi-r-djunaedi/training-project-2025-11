package com.gdn.cart.exception;

import com.gdn.cart.dto.response.ApiResponse;
import com.gdn.cart.dto.response.ErrorDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    //product not found
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetailsDTO>> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("ProductNotFoundException: {}", ex.getMessage());

        ErrorDetailsDTO error = new ErrorDetailsDTO(
                "PRODUCT_NOT_FOUND",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage(), error));
    }

    //cart not found
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetailsDTO>> handleCartNotFound(CartNotFoundException ex) {
        log.warn("CartNotFoundException: {}", ex.getMessage());

        ErrorDetailsDTO error = new ErrorDetailsDTO(
                "CART_NOT_FOUND",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage(), error));
    }


    // validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetailsDTO>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Validation error");

        log.warn("Validation error: {}", message);

        ErrorDetailsDTO error = new ErrorDetailsDTO(
                "VALIDATION_ERROR",
                message
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message, error));
    }


    // fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetailsDTO>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorDetailsDTO error = new ErrorDetailsDTO(
                "INTERNAL_ERROR",
                "Unexpected error occurred"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("Something went wrong", error));
    }
}
