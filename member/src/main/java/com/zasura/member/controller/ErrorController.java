package com.zasura.member.controller;

import com.zasura.member.dto.CommonResponse;
import com.zasura.member.exception.AuthenticationFailedException;
import com.zasura.member.exception.EmailExistException;
import com.zasura.member.exception.MemberNotFoundException;
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

  @ExceptionHandler(MemberNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<CommonResponse> handleProductNotFound(MemberNotFoundException memberNotFoundException) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(CommonResponse.builder()
            .code(HttpStatus.NOT_FOUND.value())
            .status(HttpStatus.NOT_FOUND.name())
            .success(false)
            .errorMessage(memberNotFoundException.getMessage())
            .build());

  }

  @ExceptionHandler(EmailExistException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<CommonResponse> handleEmailExist(EmailExistException emailExistException) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(CommonResponse.builder()
            .code(HttpStatus.CONFLICT.value())
            .status(HttpStatus.CONFLICT.name())
            .success(false)
            .errorMessage(emailExistException.getMessage())
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

  @ExceptionHandler(AuthenticationFailedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<CommonResponse> handleProductNotFound(AuthenticationFailedException authenticationFailedException) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(CommonResponse.builder()
            .code(HttpStatus.UNAUTHORIZED.value())
            .status(HttpStatus.UNAUTHORIZED.name())
            .success(false)
            .errorMessage(authenticationFailedException.getMessage())
            .build());

  }
}
