package com.marketplace.gateway.exception;

import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for API Gateway (WebFlux)
 */
@Slf4j
@Component
@Order(-1)
public class GatewayWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Error occurred: {}", ex.getMessage(), ex);

        ApiResponse<?> apiResponse;
        HttpStatus status;

        switch (ex) {
            case BaseException baseEx -> {
                status = HttpStatus.valueOf(baseEx.getStatusCode());
                apiResponse = ApiResponse.error(baseEx.getMessage());
            }
            case WebExchangeBindException bindEx -> {
                // Handle validation errors
                status = HttpStatus.BAD_REQUEST;
                List<String> details = new ArrayList<>();
                bindEx.getFieldErrors()
                        .forEach(error -> details.add(error.getField() + ": " + error.getDefaultMessage()));
                apiResponse = ApiResponse.error("Validation failed", details);
            }
            case ServerWebInputException serverWebInputException -> {
                // Handle JSON parsing errors and other input exceptions
                status = HttpStatus.BAD_REQUEST;
                apiResponse = ApiResponse.error("Invalid request format");
            }
            default -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                apiResponse = ApiResponse.error("An unexpected error occurred");
            }
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(apiResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing response", e);
            return exchange.getResponse().setComplete();
        }
    }
}
