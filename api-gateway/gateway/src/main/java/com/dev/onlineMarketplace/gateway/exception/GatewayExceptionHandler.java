package com.dev.onlineMarketplace.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Gateway
 * Handles rate limiting and other gateway-specific errors
 */
@Component
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // Set content type
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Add CORS headers
        addCorsHeaders(response.getHeaders());

        HttpStatus status;
        String message;
        String errorType;

        // Handle different exception types
        if (ex.getMessage() != null && ex.getMessage().contains("Too Many Requests")) {
            status = HttpStatus.TOO_MANY_REQUESTS;
            message = "Rate limit exceeded. Please try again later.";
            errorType = "RATE_LIMIT_EXCEEDED";
            
            // Add rate limit headers
            response.getHeaders().add("X-RateLimit-Retry-After-Seconds", "60");
            
            logger.warn("Rate limit exceeded for request: {}", exchange.getRequest().getPath());
        } else if (ex instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = "Requested service not found";
            errorType = "SERVICE_NOT_FOUND";
            logger.error("Service not found: {}", ex.getMessage());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred";
            errorType = "INTERNAL_ERROR";
            logger.error("Unexpected error in gateway", ex);
        }

        response.setStatusCode(status);

        // Build error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", status.value());
        errorResponse.put("error", errorType);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", exchange.getRequest().getPath().value());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing error response", e);
            bytes = "{\"error\":\"Error processing response\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private void addCorsHeaders(HttpHeaders headers) {
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    }
}

