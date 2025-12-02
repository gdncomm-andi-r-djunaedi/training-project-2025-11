package com.blibli.training.member.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = ex.getMessage().toLowerCase();
        if (msg.contains("invalid username") || msg.contains("invalid password")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (msg.contains("missing authorization")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (msg.contains("unauthorized")) {
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage()
                ));
        body.put("fields", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
