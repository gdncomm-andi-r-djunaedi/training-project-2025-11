package com.blibli.cartData.exception;

import com.blibli.cartData.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "400",
                "Bad Request: Invalid Argument",
                ex.getMessage(),
                400
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
