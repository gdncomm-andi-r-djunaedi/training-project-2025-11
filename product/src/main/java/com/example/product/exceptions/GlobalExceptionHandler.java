package com.example.product.exceptions;

import com.example.product.utils.APIResponse;
import com.example.product.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<APIResponse<Object>> handleProductNotFound(ProductNotFoundException ex) {
        APIResponse<Object> response = ResponseUtil.errorWithMessage(
                HttpStatus.NOT_FOUND.value(), 
                HttpStatus.NOT_FOUND, 
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleGenericException(Exception ex) {
        APIResponse<Object> response = ResponseUtil.errorWithMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
