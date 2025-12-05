package com.training.cartService.cartmongo.exception;

import com.training.cartService.cartmongo.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(CartException.ProductNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleProductNotFoundException(
                        CartException.ProductNotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler(CartException.InvalidRequestException.class)
        public ResponseEntity<ApiResponse<Object>> handleInvalidRequestException(
                        CartException.InvalidRequestException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler(CartException.class)
        public ResponseEntity<ApiResponse<Object>> handleCartException(CartException ex) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler(CartException.CartNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleCartNotFoundException(CartException.CartNotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Object>> handleValidationException(
                        org.springframework.web.bind.MethodArgumentNotValidException ex) {
                String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .findFirst()
                                .orElse("Validation failed");
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(errorMessage));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
        }
}
