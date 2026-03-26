package com.blibli.apiGateway.exception;

import com.blibli.apiGateway.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.validation.FieldError;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFound(FeignException.NotFound ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "404",
                "Resource Not Found",
                "The requested resource does not exist",
                404
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing + " | " + replacement));

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "400",
                "Validation Failed",
                fieldErrors.toString(),
                400
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "400",
                "Validation Failed",
                ex.getMessage(),
                400
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String errorMessage = "Email already exists. Please try a different email address.";

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "400",
                "Bad Request",
                errorMessage,
                400
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "400",
                "Bad Request",
                ex.getMessage(),
                400
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        int statusCode = ex.status();
        String errorName = (statusCode >= 400 && statusCode < 500) ? "Client Error" : "Internal Server Error";

        ErrorResponse downstreamError = null;
        try {
            downstreamError = objectMapper.readValue(ex.contentUTF8(), ErrorResponse.class);
        } catch (Exception parseEx) {
        }

        if (downstreamError != null) {
            return new ResponseEntity<>(downstreamError, HttpStatus.valueOf(statusCode));
        }

        String cleanFallbackMessage = String.format("Service communication error: Received status %d from downstream service.",
                statusCode);

        String rawContent = ex.contentUTF8();
        if (rawContent != null && !rawContent.isEmpty()) {
            cleanFallbackMessage += " Details: " + rawContent.substring(0, Math.min(150, rawContent.length())) + "...";
        }

        ErrorResponse genericErrorResponse = new ErrorResponse(
                LocalDateTime.now(),
                String.valueOf(statusCode),
                errorName,
                cleanFallbackMessage,
                statusCode
        );
        return new ResponseEntity<>(genericErrorResponse, HttpStatus.valueOf(statusCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "500",
                "Internal Server Error",
                ex.getMessage(),
                500
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        String errorMessage = ex.getMessage();

        if (errorMessage != null && (errorMessage.contains("token") || errorMessage.contains("EXPIRED") ||
                errorMessage.contains("BLACKLISTED") || errorMessage.contains("INVALID"))) {

            String message;
            switch (errorMessage) {
                case "EXPIRED" -> message = "Token has expired. Please login again.";
                case "BLACKLISTED" -> message = "Token has been invalidated. Please login again.";
                case "INVALID" -> message = "Invalid token. Please login again.";
                default -> message = "Invalid or expired token. Please login again.";
            }

            ErrorResponse error = new ErrorResponse(
                    LocalDateTime.now(),
                    "401",
                    "Unauthorized",
                    message,
                    401
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "500",
                "Internal Server Error",
                errorMessage,
                500
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}