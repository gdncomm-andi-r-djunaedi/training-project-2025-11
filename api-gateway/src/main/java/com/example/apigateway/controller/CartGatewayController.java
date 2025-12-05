package com.example.apigateway.controller;

import com.example.apigateway.common.ApiResponse;
import com.example.apigateway.dto.AddToCartRequest;
import com.example.apigateway.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cart")
@Tag(name = "Cart")
@SecurityRequirement(name = "Bearer Authentication")
public class CartGatewayController {

    private final RestTemplate restTemplate;

    @Value("${cart.service.base-url}")
    private String cartServiceUrl;

    public CartGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/addToCart")
    @Operation(summary = "Add product to cart")
    public ResponseEntity<ApiResponse<?>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String userId = principal.getUserId();

        URI uri = UriComponentsBuilder
                .fromUriString(cartServiceUrl + "/api/cart/addToCart")
                .queryParam("memberId", userId)
                .build()
                .toUri();

        ResponseEntity<Object> response = restTemplate.postForEntity(
                uri,
                request,
                Object.class
        );

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(response.getStatusCode().value())
                .status(HttpStatus.valueOf(response.getStatusCode().value()).name())
                .data(response.getBody())
                .errors(null)
                .build();

        return ResponseEntity.status(response.getStatusCode()).body(apiResponse);
    }

    @DeleteMapping("/deleteFromCart/{id}")
    @Operation(summary = "Delete cart item")
    public ResponseEntity<ApiResponse<?>> deleteFromCart(@PathVariable String id) {

        URI uri = UriComponentsBuilder
                .fromUriString(cartServiceUrl)
                .path("/api/cart/deleteFromCart/{id}")
                .buildAndExpand(id)
                .toUri();

        ResponseEntity<Boolean> response = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                null,
                Boolean.class
        );

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(response.getStatusCode().value())
                .status(HttpStatus.valueOf(response.getStatusCode().value()).name())
                .data(Map.of("deleted", response.getBody()))
                .errors(null)
                .build();

        return ResponseEntity.status(response.getStatusCode()).body(apiResponse);
    }

    @GetMapping("/getCart")
    @Operation(summary = "Get user cart")
    public ResponseEntity<ApiResponse<?>> getCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userId = principal.getUserId();

        URI uri = UriComponentsBuilder
                .fromUriString(cartServiceUrl)
                .path("/api/cart/getCart")
                .queryParam("memberId", userId)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .toUri();

        ResponseEntity<Object> response = restTemplate.getForEntity(uri, Object.class);
        Map<String, Object> body = response.getBody() instanceof Map ? (Map<String, Object>) response.getBody() : Map.of();

        Object content = body.get("content");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(response.getStatusCode().value())
                .status(HttpStatus.valueOf(response.getStatusCode().value()).name())
                .data(content)
                .errors(null)
                .build();

        return ResponseEntity.status(response.getStatusCode()).body(apiResponse);
    }
}