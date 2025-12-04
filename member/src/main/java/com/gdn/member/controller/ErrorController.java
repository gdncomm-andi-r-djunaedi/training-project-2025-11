package com.gdn.member.controller;

import com.gdn.member.exception.DataNotFoundException;
import com.gdn.member.exception.UnauthorizeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorController {

  @ExceptionHandler(UnauthorizeException.class)
  public ResponseEntity<Map<String, Object>> handleUnAuthorize(UnauthorizeException unauthorizeException) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(Map.of("code", HttpStatus.UNAUTHORIZED.value(), "status", HttpStatus.UNAUTHORIZED.name(), "message","Invalid Username or Password"));
  }

  @ExceptionHandler(DataNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(DataNotFoundException dataNotFoundException) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(Map.of("code", HttpStatus.NOT_FOUND.value(), "status", HttpStatus.NOT_FOUND.name()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
    MethodArgumentNotValidException methodArgumentNotValidException) {
    final Map<String, List<String>> errors = methodArgumentNotValidException.getBindingResult()
      .getFieldErrors()
      .stream()
      .collect(Collectors.groupingBy(
        FieldError::getField,
        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
      ));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(Map.of("code", HttpStatus.BAD_REQUEST.value(), "status", HttpStatus.BAD_REQUEST.name(), "errors", errors));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
      .body(Map.of(
        "code", HttpStatus.CONFLICT.value(),
        "status", HttpStatus.CONFLICT.name(),
        "message", "Username already exists"
      ));
  }
}
