package com.yuna.member.advice;

import com.yuna.member.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(RuntimeException ex) {
    return new ResponseEntity<>(ErrorResponse.builder().message(ex.getMessage()).status("NOT_OK").build(), HttpStatus.I_AM_A_TEAPOT);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(RuntimeException ex) {
    return new ResponseEntity<>(ErrorResponse.builder().message(ex.getMessage()).status("NOT_OK").build(), HttpStatus.NOT_FOUND);
  }
}
