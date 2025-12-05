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
@RequestMapping("/gateway/products")
@RequiredArgsConstructor
public class ProductGatewayController {

    private final WebClient.Builder webClientBuilder;

    @Value("${product.service.url}")
    private String productServiceUrl;

    private WebClient getClient(String token) {
        return webClientBuilder.build()
                .mutate()
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    // -------------------------------
    // SEARCH
    // -------------------------------
    @GetMapping("/search")
    public Mono<ResponseEntity<String>> searchProducts(
            ServerWebExchange request,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");

        return getClient(extractToken(authHeader))
                .get()
                .uri(productServiceUrl + "/searchByProductName?keyword={k}&page={p}&size={s}",
                        keyword, page, size)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    // -------------------------------
    // CREATE PRODUCT
    // -------------------------------
    @PostMapping("/create")
    public Mono<ResponseEntity<String>> createProduct(
            ServerWebExchange request,
            @RequestBody String productJson
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");
        return getClient(extractToken(authHeader))
                .post()
                .uri(productServiceUrl + "/createNewProduct")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productJson)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    // -------------------------------
    // GET BY ID
    // -------------------------------
    @GetMapping("/{id}")
    public Mono<ResponseEntity<String>> getById(
            ServerWebExchange request,
            @PathVariable UUID id
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");
        return getClient(extractToken(authHeader))
                .get()
                .uri(productServiceUrl + "/getProductById/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    // -------------------------------
    // UPDATE PRODUCT
    // -------------------------------
    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<String>> update(
            ServerWebExchange request,
            @PathVariable UUID id,
            @RequestBody String productJson
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");
        return getClient(extractToken(authHeader))
                .put()
                .uri(productServiceUrl + "/updateProductById/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productJson)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    // -------------------------------
    // DELETE PRODUCT
    // -------------------------------
    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> delete(
            ServerWebExchange request,
            @PathVariable UUID id
    ) {
        String authHeader = request.getRequest().getHeaders().getFirst("Authorization");
        return getClient(extractToken(authHeader))
                .delete()
                .uri(productServiceUrl + "/deleteProductById/" + id)
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> ResponseEntity.noContent().build());
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
