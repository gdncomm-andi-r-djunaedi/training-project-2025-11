package com.zasura.product.controller;

import com.zasura.product.dto.CommonResponse;
import com.zasura.product.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<CommonResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException httpMessageNotReadableException) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(CommonResponse.builder()
            .code(HttpStatus.BAD_REQUEST.value())
            .status(HttpStatus.BAD_REQUEST.name())
            .success(false)
            .errorMessage(httpMessageNotReadableException.getMessage())
            .build());
  }

}
