package com.blibi.cart.exception;

import com.blibi.cart.dto.GenericResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Cart service.
 * 
 * This class provides centralized exception handling for all controllers
 * in the application. It ensures consistent error response format
 * across all endpoints and appropriate HTTP status codes.
 * 
 * Key Features:
 * - Centralized error handling using @RestControllerAdvice
 * - Consistent error response format (GenericResponse)
 * - Appropriate HTTP status codes for different error types
 * - Comprehensive logging for debugging
 * - Handles multiple exception types
 * 
 * Exception Handling Strategy:
 * - Custom exceptions (CartNotFoundException)
 * - Feign client exceptions (service communication errors)
 * - Validation errors (MethodArgumentNotValidException)
 * - Type mismatch errors (MethodArgumentTypeMismatchException)
 * - Illegal argument exceptions
 * - General exceptions (catch-all)
 * 
 * @RestControllerAdvice: Makes this class handle exceptions from all controllers
 * @Slf4j: Provides logging functionality
 * 
 * @author Cart Service Team
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles CartNotFoundException.
     * 
     * This exception is thrown when operations are attempted on a cart
     * that doesn't exist in the database.
     * 
     * Response Details:
     * - HTTP Status: 404 NOT FOUND
     * - Status: "ERROR"
     * - Message: Exception message
     * - Data: null
     * 
     * @param ex The CartNotFoundException that was thrown
     * @return ResponseEntity with error response and 404 status
     */
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<GenericResponse<Object>> handleCartNotFound(CartNotFoundException ex) {
        log.error("Cart not found: {}", ex.getMessage());

        // Build error response with consistent structure
        GenericResponse<Object> response = GenericResponse.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles Feign client exceptions.
     * 
     * This exception is thrown when communication with Product Service fails
     * or when Product Service returns an error response.
     * 
     * Error Mapping:
     * - 404: Product not found in Product Service -> 404 NOT FOUND
     * - 500+: Product Service server error -> 503 SERVICE UNAVAILABLE
     * - Other errors: Communication error -> 502 BAD GATEWAY
     * 
     * Response Details:
     * - Status: "ERROR"
     * - Message: Descriptive error message based on Feign exception status
     * - Data: null
     * 
     * @param ex The FeignException that was thrown
     * @return ResponseEntity with error response and appropriate HTTP status
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<GenericResponse<Object>> handleFeignError(FeignException ex) {
        log.error("Feign client error: {} - {}", ex.status(), ex.getMessage());

        // Determine appropriate error message and HTTP status based on Feign exception status
        String message;
        HttpStatus status;

        // Map Feign exception status to appropriate HTTP status and message
        if (ex.status() == 404) {
            // Product not found in Product Service
            message = "Product not found in product service";
            status = HttpStatus.NOT_FOUND;
        } else if (ex.status() >= 500) {
            // Product Service server error
            message = "Product service is currently unavailable. Please try again later.";
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            // Other communication errors
            message = "Error communicating with product service";
            status = HttpStatus.BAD_GATEWAY;
        }

        // Build error response with consistent structure
        GenericResponse<Object> response = GenericResponse.builder()
                .status("ERROR")
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Handles validation errors from request body validation.
     * 
     * This exception is thrown when @Valid annotation validation fails
     * on request parameters or request body objects.
     * 
     * Response Details:
     * - HTTP Status: 400 BAD REQUEST
     * - Status: "ERROR"
     * - Message: "Validation failed"
     * - Data: Map of field names to error messages
     * 
     * Example Response:
     * {
     *   "status": "ERROR",
     *   "message": "Validation failed",
     *   "data": {
     *     "quantity": "Quantity should not be less than 0",
     *     "totalCartPrice": "Price must be greater than zero"
     *   }
     * }
     * 
     * @param ex The MethodArgumentNotValidException that was thrown
     * @return ResponseEntity with validation errors and 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        // Extract field validation errors into a map
        // Key: field name, Value: error message
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        // Build error response with validation errors in data field
        GenericResponse<Map<String, String>> response = GenericResponse.<Map<String, String>>builder()
                .status("ERROR")
                .message("Validation failed")
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles type mismatch errors in path variables or request parameters.
     * 
     * This exception is thrown when a path variable or request parameter
     * cannot be converted to the expected type (e.g., invalid UUID format).
     * 
     * Common Scenarios:
     * - Invalid UUID format in path variable
     * - Invalid number format
     * - Invalid date format
     * 
     * Response Details:
     * - HTTP Status: 400 BAD REQUEST
     * - Status: "ERROR"
     * - Message: Descriptive message with parameter name, value, and expected type
     * - Data: null
     * 
     * Example:
     * Invalid UUID "invalid-uuid" for parameter "userId". Expected type: UUID
     * 
     * @param ex The MethodArgumentTypeMismatchException that was thrown
     * @return ResponseEntity with error response and 400 status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericResponse<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch error: {}", ex.getMessage());

        // Build descriptive error message with parameter details
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        // Build error response with consistent structure
        GenericResponse<Object> response = GenericResponse.builder()
                .status("ERROR")
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles IllegalArgumentException.
     * 
     * This exception is thrown when invalid arguments are passed to methods,
     * such as null values where not allowed or invalid parameter combinations.
     * 
     * Response Details:
     * - HTTP Status: 400 BAD REQUEST
     * - Status: "ERROR"
     * - Message: Exception message
     * - Data: null
     * 
     * @param ex The IllegalArgumentException that was thrown
     * @return ResponseEntity with error response and 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());

        // Build error response with exception message
        GenericResponse<Object> response = GenericResponse.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles all other unhandled exceptions (catch-all handler).
     * 
     * This is a fallback handler for any exceptions that don't match
     * the specific exception handlers above. It prevents exposing
     * internal error details to clients.
     * 
     * Response Details:
     * - HTTP Status: 500 INTERNAL SERVER ERROR
     * - Status: "ERROR"
     * - Message: Generic error message (doesn't expose internal details)
     * - Data: null
     * 
     * Security Note:
     * The error message is generic to avoid exposing internal system
     * details that could be used for attacks. Full error details are
     * logged for debugging purposes.
     * 
     * @param ex The Exception that was thrown
     * @return ResponseEntity with error response and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<Object>> handleGeneralException(Exception ex) {
        // Log full exception details for debugging (not exposed to client)
        log.error("Unexpected error occurred", ex);

        // Build generic error response (doesn't expose internal details)
        GenericResponse<Object> response = GenericResponse.builder()
                .status("ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
