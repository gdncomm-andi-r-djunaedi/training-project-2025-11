package com.example.product.exception;

import com.example.common.model.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ProductExceptionHandler {

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(ProductNotFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ApiError body =
        new ApiError(Instant.now(), status.value(), "PRODUCT_NOT_FOUND", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST; // 400
    ApiError body =
        new ApiError(Instant.now(), status.value(), "VALIDATION_ERROR", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
    ApiError body = new ApiError(Instant.now(),
        status.value(),
        "INTERNAL_ERROR",
        "Unexpected error occurred",
        request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}
