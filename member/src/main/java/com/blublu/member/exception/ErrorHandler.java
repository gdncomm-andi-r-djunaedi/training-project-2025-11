package com.blublu.member.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ControllerAdvice
public class ErrorHandler {
  @ExceptionHandler(UsernameExistException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Map<String, Object>> dataNotFoundHandler(UsernameExistException usernameExistException) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("code",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "errorMessage",
            usernameExistException.getMessage()));
  }

  @ExceptionHandler(UsernameNotExistException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Map<String, Object>> dataNotFoundHandler(UsernameNotExistException usernameNotExistException) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("code",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "errorMessage",
            usernameNotExistException.getMessage()));
  }
}
