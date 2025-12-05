package com.elfrida.api_gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;

@RestController
@RequestMapping("/api/products")
public class ProductGatewayController {

    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String PRODUCT_SERVICE_URL;

    public ProductGatewayController() {
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    @GetMapping
    public ResponseEntity<String> getAllProducts(HttpServletRequest request) {
        return forwardRequest(PRODUCT_SERVICE_URL, HttpMethod.GET, request, null);
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchProducts(HttpServletRequest request) {
        return forwardRequest(PRODUCT_SERVICE_URL + "/search", HttpMethod.GET, request, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable String id, HttpServletRequest request) {
        return forwardRequest(PRODUCT_SERVICE_URL + "/" + id, HttpMethod.GET, request, null);
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody(required = false) String body,
            HttpServletRequest request) {
        return forwardRequest(PRODUCT_SERVICE_URL, HttpMethod.POST, request, body);
    }

    private ResponseEntity<String> forwardRequest(String url, HttpMethod method, HttpServletRequest request,
            String body) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.add(headerName, request.getHeader(headerName));
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        String query = request.getQueryString();
        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .query(query)
                .build(true)
                .toUri();

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, method, httpEntity, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Gateway Error: " + e.getMessage());
        }
    }
}
