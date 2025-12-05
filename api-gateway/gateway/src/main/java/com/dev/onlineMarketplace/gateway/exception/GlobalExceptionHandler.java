package com.dev.onlineMarketplace.gateway.exception;

import com.dev.onlineMarketplace.gateway.response.GdnResponseData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ex.printStackTrace(); // Log error for debugging
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();

        if (ex instanceof io.jsonwebtoken.security.SignatureException
                || ex instanceof io.jsonwebtoken.ExpiredJwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Invalid or expired token";
        } else if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            status = HttpStatus
                    .valueOf(((org.springframework.web.server.ResponseStatusException) ex).getStatusCode().value());
            message = ex.getMessage();
        }

        exchange.getResponse().setStatusCode(status);
        org.springframework.http.HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Add CORS headers to error responses
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");

        GdnResponseData<Object> response = GdnResponseData.error(status.value(), message);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        try {
            DataBuffer buffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(response));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
