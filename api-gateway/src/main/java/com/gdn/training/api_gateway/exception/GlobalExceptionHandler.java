package com.gdn.training.api_gateway.exception;

import com.gdn.training.common.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        log.warn("Invalid request at {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(buildError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex,
                                                            HttpServletRequest request) {
        log.error("Service state error at {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildError(HttpStatus.SERVICE_UNAVAILABLE,
                        "Service Unavailable",
                        "Please try again shortly.",
                        request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed at {}: {}", request.getRequestURI(), detail);
        return ResponseEntity.badRequest().body(buildError(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                detail,
                request
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        log.error("Unexpected error at {}:", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "An unexpected error occurred. Please try again later.",
                        request
                ));
    }

    private ErrorResponse buildError(HttpStatus status,
                                     String error,
                                     String message,
                                     HttpServletRequest request) {
        return ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
    }
}

