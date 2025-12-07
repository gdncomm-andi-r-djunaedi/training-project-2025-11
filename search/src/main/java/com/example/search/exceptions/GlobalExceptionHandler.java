package com.example.search.exceptions;

import com.example.search.utils.APIResponse;
import com.example.search.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<APIResponse<Object>> handleProductNotFound(ProductNotFoundException ex) {
        APIResponse<Object> response = ResponseUtil.error(
                ex.getMessage(),
                String.valueOf(HttpStatus.NOT_FOUND.value())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(SearchException.class)
    public ResponseEntity<APIResponse<Object>> handleSearchException(SearchException ex) {
        APIResponse<Object> response = ResponseUtil.error(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleGenericException(Exception ex) {
        APIResponse<Object> response = ResponseUtil.error(
                "An unexpected error occurred: " + ex.getMessage(),
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
