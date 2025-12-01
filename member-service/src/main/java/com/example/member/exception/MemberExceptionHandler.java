package com.example.member.exception;

import com.example.common.model.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class MemberExceptionHandler {

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ApiError> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.CONFLICT; // 409
    ApiError body =
        new ApiError(Instant.now(), status.value(), "DUPLICATE_EMAIL", ex.getMessage(), request.getRequestURI());
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

    // 🔥 Important: log full stack trace so we can see cause of /v3/api-docs failure
    log.error("Unhandled exception on path {}: {}", request.getRequestURI(), ex.getMessage(), ex);

    ApiError body = new ApiError(Instant.now(), status.value(), "INTERNAL_ERROR",
        // For debugging we expose ex.getMessage(); later you can replace with generic text again
        ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}