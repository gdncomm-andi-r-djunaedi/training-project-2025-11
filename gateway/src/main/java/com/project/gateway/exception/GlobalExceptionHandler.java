package com.project.gateway.exception;

import com.project.gateway.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Global Exception Handler for Gateway
 * Handles all exceptions and returns standardized error responses
 */
@Slf4j
@Component
@Order(-2) // Higher priority than default error handler
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Exception occurred: {}", ex.getMessage(), ex);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal Server Error";

        if (ex instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            message = responseStatusException.getReason() != null 
                    ? responseStatusException.getReason() 
                    : status.getReasonPhrase();
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
        } else if (ex instanceof SecurityException) {
            status = HttpStatus.FORBIDDEN;
            message = ex.getMessage();
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getURI().getPath())
                .build();

        String jsonResponse = convertToJson(errorResponse);
        DataBuffer dataBuffer = bufferFactory.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private String convertToJson(ErrorResponse errorResponse) {
        return String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                errorResponse.getTimestamp(),
                errorResponse.getStatus(),
                errorResponse.getError(),
                errorResponse.getMessage(),
                errorResponse.getPath()
        );
    }
}

