package com.example.apigateway.config;


import com.example.apigateway.common.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpClientError(HttpClientErrorException ex) {
        String message = extractMessage(ex);
        int code = ex.getStatusCode().value();

        ApiResponse<?> resp = ApiResponse.builder()
                .code(code)
                .status(HttpStatus.valueOf(code).name())
                .data(null)
                .errors(message)
                .build();

        return ResponseEntity.status(code).body(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAll(Exception ex) {
        ApiResponse<?> resp = ApiResponse.builder()
                .code(500)
                .status("INTERNAL_SERVER_ERROR")
                .data(null)
                .errors(ex.getMessage())
                .build();

        return ResponseEntity.status(500).body(resp);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        ApiResponse<?> resp = ApiResponse.builder()
                .code(400)
                .status("BAD_REQUEST")
                .data(null)
                .errors(message)
                .build();

        return ResponseEntity.badRequest().body(resp);
    }

    private String extractMessage(HttpClientErrorException ex) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode body = mapper.readTree(ex.getResponseBodyAsString());

            if (body.has("message")) {
                return body.get("message").asText();
            }
        } catch (Exception ignored) {
        }

        return ex.getMessage();
    }
}

