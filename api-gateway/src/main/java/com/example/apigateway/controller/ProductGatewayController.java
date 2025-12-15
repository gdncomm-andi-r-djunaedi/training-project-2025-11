package com.example.apigateway.controller;

import com.example.apigateway.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/product")
@Tag(name = "Product")
public class ProductGatewayController {

    private final RestTemplate restTemplate;

    @Value("${product.service.base-url}")
    private String productServiceUrl;

    public ProductGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/lists")
    @Operation(summary = "Get all products")
    public ResponseEntity<ApiResponse<?>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        URI uri = UriComponentsBuilder
                .fromUriString(productServiceUrl)
                .path("/api/product/lists")
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

    @GetMapping("/search")
    @Operation(summary = "Search products")
    public ResponseEntity<ApiResponse<?>> searchProducts(
            @RequestParam(defaultValue = "string") String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        URI uri = UriComponentsBuilder
                .fromUriString(productServiceUrl)
                .path("/api/product/search")
                .queryParam("productName", productName)
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

    @GetMapping("/product-detail/{id}")
    @Operation(summary = "Get product detail")
    public ResponseEntity<ApiResponse<?>> getProductDetail(@PathVariable String id) {

        URI uri = UriComponentsBuilder
                .fromUriString(productServiceUrl)
                .path("/api/product/product-detail/{id}")
                .buildAndExpand(id)
                .toUri();

        ResponseEntity<Object> response = restTemplate.getForEntity(uri, Object.class);

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(response.getStatusCode().value())
                .status(HttpStatus.valueOf(response.getStatusCode().value()).name())
                .data(response.getBody())
                .errors(null)
                .build();

        return ResponseEntity.status(response.getStatusCode()).body(apiResponse);
    }
}