package com.zasura.cart.controller;

import com.zasura.cart.dto.CommonResponse;
import com.zasura.cart.exception.CartEmptyException;
import com.zasura.cart.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorController {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<CommonResponse> handleMethodArgumentNotValidFound(
      MethodArgumentNotValidException methodArgumentNotValidException) {
    final Map<String, List<String>> errors = methodArgumentNotValidException.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.groupingBy(FieldError::getField,
            Collectors.mapping(fieldError -> fieldError.getDefaultMessage(), Collectors.toList())));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(CommonResponse.builder()
            .code(HttpStatus.BAD_REQUEST.value())
            .status(HttpStatus.BAD_REQUEST.name())
            .success(false)
            .errorMessage(errors)
            .build());
  }

  @ExceptionHandler(ProductNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<CommonResponse> handleProductNotFound(ProductNotFoundException productNotFoundException) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(CommonResponse.builder()
            .code(HttpStatus.NOT_FOUND.value())
            .status(HttpStatus.NOT_FOUND.name())
            .success(false)
            .errorMessage(productNotFoundException.getMessage())
            .build());
  }

  @ExceptionHandler(CartEmptyException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<CommonResponse> handleCartEmpty(CartEmptyException cartEmptyException) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(CommonResponse.builder()
            .code(HttpStatus.NOT_FOUND.value())
            .status(HttpStatus.NOT_FOUND.name())
            .success(false)
            .errorMessage(cartEmptyException.getMessage())
            .build());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<CommonResponse> handleIllegalArgument(IllegalArgumentException illegalArgumentException) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(CommonResponse.builder()
            .code(HttpStatus.NOT_FOUND.value())
            .status(HttpStatus.NOT_FOUND.name())
            .success(false)
            .errorMessage(illegalArgumentException.getLocalizedMessage())
            .build());
  }
}
