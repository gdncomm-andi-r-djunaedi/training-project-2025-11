package com.example.api_gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed: " + errors.toString(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenException(TokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "TOKEN_ERROR",
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidCredentialsExceptionToken.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsToken(InvalidCredentialsExceptionToken ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex) {
        return new ResponseEntity<>(ex.getErrorResponse(), ex.getHttpStatus());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        try {
            if (ex.responseBody().isPresent()) {
                ByteBuffer byteBuffer = ex.responseBody().get();
                if (byteBuffer != null && byteBuffer.hasRemaining()) {
                    byte[] bodyBytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bodyBytes);
                    String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
                    ObjectMapper objectMapper = new ObjectMapper();
                    ErrorResponse errorResponse = objectMapper.readValue(bodyString, ErrorResponse.class);
                    HttpStatus httpStatus = HttpStatus.valueOf(ex.status());
                    return new ResponseEntity<>(errorResponse, httpStatus);
                }
            }
        } catch (Exception e) {
        }
        ErrorResponse errorResponse = new ErrorResponse(
                "SERVICE_ERROR",
                ex.getMessage(),
                System.currentTimeMillis()
        );
        HttpStatus httpStatus = ex.status() > 0 ? HttpStatus.valueOf(ex.status()) : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred: " + ex.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}