package com.blibli.cartmodule.controller.exception;

import com.blibli.cartmodule.dto.CartResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice(basePackages = "com.blibli.cartmodule.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CartResponseDto> handleResponseStatusException(ResponseStatusException e) {
        log.warn("ResponseStatusException: Status: {}, Message: {}", e.getStatus(), e.getReason());
        CartResponseDto errorResponse = new CartResponseDto();
        errorResponse.setStatus("ERROR");
        errorResponse.setMessage(e.getReason());
        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CartResponseDto> handleGenericException(Exception e) {
        log.error("Unexpected exception: Exception Type: {}, Message: {}", 
                e.getClass().getName(), e.getMessage(), e);
        CartResponseDto errorResponse = new CartResponseDto();
        errorResponse.setStatus("ERROR");
        errorResponse.setMessage("Error processing request: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}


