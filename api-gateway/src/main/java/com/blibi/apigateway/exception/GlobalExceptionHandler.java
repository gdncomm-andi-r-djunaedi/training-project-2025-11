package com.blibi.apigateway.exception;

import com.blibi.apigateway.dto.GenericResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public GenericResponse<?> handleAny(Exception ex) {
        return GenericResponse.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .build();
    }
}
