package com.example.cart.exceptions;

import com.example.cart.utils.APIResponse;
import com.example.cart.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        APIResponse response = ResponseUtil.errorWithMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleGeneralException(Exception ex) {
        APIResponse response = ResponseUtil.errorWithMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
