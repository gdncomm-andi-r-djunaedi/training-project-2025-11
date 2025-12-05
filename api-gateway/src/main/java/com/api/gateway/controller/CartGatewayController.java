package com.api.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/gateway/cart")
@RequiredArgsConstructor
public class CartGatewayController {

    private final WebClient.Builder webClientBuilder;

    // Your Cart Microservice base URL
    @Value("${cart.service.url}")
    private String cartServiceUrl;

    private WebClient getClient(String token) {
        return webClientBuilder.build()
                .mutate()
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    // ------------------------
    // GET CART
    // ------------------------
    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<?>> getCart(
            ServerWebExchange request,
            @PathVariable UUID customerId
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");

        return getClient(extractToken(authHeader))
                .get()
                .uri(cartServiceUrl + "/" + customerId)
                .retrieve()
                .bodyToMono(Object.class)
                .map(ResponseEntity::ok);
    }

    // ------------------------
    // ADD ITEM TO CART
    // ------------------------
    @PostMapping("/{customerId}/add")
    public Mono<ResponseEntity<?>> addItem(
            ServerWebExchange request,
            @PathVariable UUID customerId,
            @RequestBody Object requestBody
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");

        return getClient(extractToken(authHeader))
                .post()
                .uri(cartServiceUrl + "/" + customerId + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Object.class)
                .map(ResponseEntity::ok);
    }

    // ------------------------
    // UPDATE ITEM QUANTITY
    // ------------------------
    @PutMapping("/{customerId}/update")
    public Mono<ResponseEntity<?>> updateQuantity(
            ServerWebExchange request,
            @PathVariable UUID customerId,
            @RequestBody Object requestBody
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");

        return getClient(extractToken(authHeader))
                .put()
                .uri(cartServiceUrl + "/" + customerId + "/update")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Object.class)
                .map(ResponseEntity::ok);
    }

    // ------------------------
    // REMOVE ITEM FROM CART
    // ------------------------
    @DeleteMapping("/{customerId}/remove")
    public Mono<ResponseEntity<?>> removeItem(
            ServerWebExchange request,
            @PathVariable UUID customerId,
            @RequestBody Object requestBody
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");

        return getClient(extractToken(authHeader))
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri(cartServiceUrl + "/" + customerId + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Object.class)
                .map(ResponseEntity::ok);
    }

    // ------------------------
    // CLEAR CART
    // ------------------------
    @DeleteMapping("/{customerId}/clear")
    public Mono<ResponseEntity<?>> clearCart(
            ServerWebExchange request,
            @PathVariable UUID customerId
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");

        return getClient(extractToken(authHeader))
                .delete()
                .uri(cartServiceUrl + "/" + customerId + "/clear")
                .retrieve()
                .bodyToMono(Object.class)
                .map(ResponseEntity::ok);
    }

    // -------------------------------
    // Extract token from "Bearer xxxxx"
    // -------------------------------
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization header is required"
            );
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization header must start with 'Bearer '"
            );
        }

        return authorizationHeader.substring(7);
    }
}
