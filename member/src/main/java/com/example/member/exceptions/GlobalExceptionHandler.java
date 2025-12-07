package com.example.member.exceptions;

import com.example.member.utils.APIResponse;
import com.example.member.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        APIResponse response = ResponseUtil.error(ex.getMessage(), String.valueOf(HttpStatus.NOT_FOUND.value()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<APIResponse<Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        APIResponse response = ResponseUtil.error(ex.getMessage(), String.valueOf(HttpStatus.CONFLICT.value()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<APIResponse<Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        APIResponse response = ResponseUtil.error(ex.getMessage(), String.valueOf(HttpStatus.UNAUTHORIZED.value()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<APIResponse<Object>> handleInvalidPassword(InvalidPasswordException ex) {
        APIResponse response = ResponseUtil.error(ex.getMessage(), String.valueOf(HttpStatus.BAD_REQUEST.value()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<Object>> handleInvalidArgument(IllegalArgumentException ex) {
        APIResponse response = ResponseUtil.error(ex.getMessage(), String.valueOf(HttpStatus.BAD_REQUEST.value()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        String errorMessage = errors.values().stream().findFirst().orElse("Validation failed");
        APIResponse response = ResponseUtil.error(errorMessage, String.valueOf(HttpStatus.BAD_REQUEST.value()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
