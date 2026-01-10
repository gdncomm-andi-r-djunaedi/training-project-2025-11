package com.blibli.apigateway.exception;

import com.blibli.apigateway.dto.response.ErrorResponse;
import com.blibli.apigateway.dto.response.LoginResponse;
import com.blibli.apigateway.dto.response.MemberErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice(basePackages = "com.blibli.apigateway.controller")
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleTokenValidationException(TokenValidationException e, WebRequest request) {
        String path = e.getPath() != null ? e.getPath() : request.getDescription(false).replace("uri=", "");
        log.warn("Token validation failed: Message: {}, Path: {}", e.getMessage(), path);
        
        String errorMessage;
        if ("EXPIRED".equals(e.getMessage())) {
            errorMessage = "Token has expired. Please login again.";
        } else if ("BLACKLISTED".equals(e.getMessage())) {
            errorMessage = "Token has been invalidated. Please login again.";
        } else {
            errorMessage = "Invalid token. Please login again.";
        }
        
        ErrorResponse errorResponse = new ErrorResponse("UNAUTHORIZED", 401, errorMessage, path);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<?> handleFeignBadRequest(FeignException.BadRequest e, WebRequest request) {
        log.warn("Bad request from service: Status: {}, Message: {}", e.status(), e.contentUTF8());
        String errorContent = e.contentUTF8();
        
        if (errorContent != null && !errorContent.trim().isEmpty()) {
            try {
                MemberErrorResponse errorResponse = objectMapper.readValue(errorContent, MemberErrorResponse.class);
                log.debug("Parsed MemberErrorResponse: error={}, message={}", errorResponse.getError(), errorResponse.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } catch (Exception parseException) {
                log.debug("Could not parse as ErrorResponse, returning raw message");
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorContent != null ? errorContent : "Bad request: " + e.getMessage());
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleFeignNotFound(FeignException.NotFound e, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        log.warn("Resource not found: Status: {}, Message: {}", e.status(), e.contentUTF8());
        
        String errorMessage = e.contentUTF8();
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = "Resource not found: " + e.getMessage();
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "NOT_FOUND",
            HttpStatus.NOT_FOUND.value(),
            errorMessage,
            path
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<?> handleFeignException(FeignException e, WebRequest request) {
        int statusCode = e.status();
        log.error("Feign exception: Status: {}, Message: {}, Exception Type: {}", 
                statusCode, e.getMessage(), e.getClass().getSimpleName());
        
        if (statusCode == 409) {
            String errorContent = e.contentUTF8();
            if (errorContent != null && !errorContent.trim().isEmpty()) {
                try {
                    MemberErrorResponse errorResponse = objectMapper.readValue(errorContent, MemberErrorResponse.class);
                    log.debug("Parsed MemberErrorResponse for conflict: error={}, message={}", 
                            errorResponse.getError(), errorResponse.getMessage());
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
                } catch (Exception parseException) {
                    log.debug("Could not parse as MemberErrorResponse, returning raw message");
                }
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MemberErrorResponse("EMAIL_ALREADY_EXISTS", 
                            errorContent != null ? errorContent : "Email already exists"));
        }
        
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null || statusCode <= 0) {
            log.error("Service connection failed or returned invalid status: {}. Full exception: {}", 
                    statusCode, e.getClass().getName());
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        String errorMessage = e.contentUTF8();
        String responseMessage = errorMessage != null ? errorMessage : "Service error: " + e.getMessage();
        if (statusCode <= 0) {
            responseMessage = "Service is unavailable. Connection failed: " + e.getMessage();
        }
        
        if (statusCode <= 0 || status == HttpStatus.SERVICE_UNAVAILABLE) {
            return ResponseEntity.status(status).body(responseMessage);
        }
        
        return ResponseEntity.status(status).body(responseMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        log.error("Runtime exception: Message: {}, Path: {}", e.getMessage(), path, e);
        
        String errorMessage = e.getMessage();
        if (errorMessage != null && (errorMessage.contains("token") || errorMessage.contains("Token") || 
            errorMessage.equals("EXPIRED") || errorMessage.equals("BLACKLISTED") || errorMessage.equals("INVALID"))) {
            String message;
            if ("EXPIRED".equals(errorMessage)) {
                message = "Token has expired. Please login again.";
            } else if ("BLACKLISTED".equals(errorMessage)) {
                message = "Token has been invalidated. Please login again.";
            } else if ("INVALID".equals(errorMessage)) {
                message = "Invalid token. Please login again.";
            } else {
                message = "Invalid or expired token. Please login again.";
            }
            ErrorResponse errorResponse = new ErrorResponse("UNAUTHORIZED", 401, message, path);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        if (errorMessage != null && errorMessage.contains("Login failed") || 
            errorMessage != null && (errorMessage.contains("NOT_FOUND") || errorMessage.contains("INVALID_PASSWORD"))) {
            LoginResponse errorResponse = new LoginResponse(null, errorMessage, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", 500, 
                "An error occurred: " + errorMessage, path);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        log.error("Unexpected exception: Exception Type: {}, Message: {}, Path: {}", 
                e.getClass().getName(), e.getMessage(), path, e);
        
        String errorMessage = e.getMessage();
        if (errorMessage != null && (errorMessage.contains("Connection refused") || 
            errorMessage.contains("connect") || errorMessage.contains("timeout"))) {
            ErrorResponse errorResponse = new ErrorResponse("SERVICE_UNAVAILABLE", 503, 
                    "Service is unavailable. Connection failed: " + errorMessage, path);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
        
        if (errorMessage != null && errorMessage.contains("Login failed")) {
            LoginResponse errorResponse = new LoginResponse(null, "Login failed: " + errorMessage, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
        
        ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", 500, 
                "An unexpected error occurred: " + errorMessage, path);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

