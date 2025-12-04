package com.blublu.cart.exception;

import com.blublu.cart.model.response.GenericBodyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorHandler {
  @ExceptionHandler(ProductNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<GenericBodyResponse> dataNotFoundHandler(ProductNotFoundException productNotFoundException) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(GenericBodyResponse.builder()
            .success(false)
            .errorMessage(productNotFoundException.getMessage())
            .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build());
  }
}
