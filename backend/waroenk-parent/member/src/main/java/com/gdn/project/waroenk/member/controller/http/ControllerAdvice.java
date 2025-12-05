package com.gdn.project.waroenk.member.controller.http;

import com.gdn.project.waroenk.member.dto.ErrorResponseDto;
import com.gdn.project.waroenk.member.exceptions.DuplicateResourceException;
import com.gdn.project.waroenk.member.exceptions.InvalidCredentialsException;
import com.gdn.project.waroenk.member.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.member.exceptions.ValidationException;
import com.gdn.project.waroenk.member.utility.ExceptionTranslatorUtil;
import io.grpc.StatusRuntimeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Order(2)
@RestControllerAdvice(basePackages = "com.gdn.project.waroenk.member.controller.http")
public class ControllerAdvice {

  // ==================== Client Errors (4xx) ====================

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("Invalid argument: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationException(ValidationException ex) {
    log.warn("Validation error: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex) {
    String violations = ex.getConstraintViolations().stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.joining(", "));
    log.warn("Constraint violation: {}", violations);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, violations);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    String errors = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    log.warn("Validation errors: {}", errors);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, errors);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponseDto> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex) {
    log.warn("Missing request parameter: {}", ex.getParameterName());
    return buildErrorResponse(HttpStatus.BAD_REQUEST,
        "Required parameter '" + ex.getParameterName() + "' is missing");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    log.warn("Type mismatch for parameter: {}", ex.getName());
    return buildErrorResponse(HttpStatus.BAD_REQUEST,
        "Invalid value for parameter '" + ex.getName() + "'");
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponseDto> handleDuplicateResourceException(DuplicateResourceException ex) {
    log.warn("Duplicate resource: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMessage());
    String message = "Data conflict occurred";
    if (ex.getMessage() != null) {
      if (ex.getMessage().contains("duplicate") || ex.getMessage().contains("unique")) {
        message = "Resource already exists";
      } else if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("fk_")) {
        message = "Referenced resource not found or cannot be deleted";
      }
    }
    return buildErrorResponse(HttpStatus.CONFLICT, message);
  }

  // ==================== Authentication/Authorization Errors ====================

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponseDto> handleInvalidCredentialsException(InvalidCredentialsException ex) {
    log.warn("Invalid credentials attempt");
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(BadCredentialsException ex) {
    log.warn("Bad credentials attempt");
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied");
  }

  // ==================== gRPC Error Translation ====================

  @ExceptionHandler(StatusRuntimeException.class)
  public ResponseEntity<ErrorResponseDto> handleStatusRuntimeException(StatusRuntimeException ex) {
    return ExceptionTranslatorUtil.translateGrpcRuntimeException(ex);
  }

  // ==================== Connection/Network Errors ====================

  @ExceptionHandler(ClientAbortException.class)
  public void handleClientAbortException(ClientAbortException ex) {
    // Client closed connection - not an error, just log at debug level
    log.debug("Client aborted connection: {}", ex.getMessage());
  }

  // ==================== Server Errors (5xx) ====================

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponseDto> handleIllegalStateException(IllegalStateException ex) {
    log.error("Illegal state: {}", ex.getMessage(), ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Operation cannot be performed in current state");
  }

  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<ErrorResponseDto> handleNullPointerException(NullPointerException ex) {
    log.error("Null pointer exception", ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred");
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
    log.error("Runtime exception: {}", ex.getMessage(), ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }

  // ==================== Helper Method ====================

  private ResponseEntity<ErrorResponseDto> buildErrorResponse(HttpStatus status, String message) {
    ErrorResponseDto errorResponse = new ErrorResponseDto(
        status.value(),
        status.name(),
        message,
        LocalDateTime.now());
    return ResponseEntity
        .status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body(errorResponse);
  }
}
