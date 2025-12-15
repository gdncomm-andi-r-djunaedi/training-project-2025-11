package com.elfrida.api_gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Enumeration;

@RestController
@RequestMapping("/api/cart")
public class CartGatewayController {

    private final RestTemplate restTemplate;

    @Value("${cart.service.url}")
    private String CART_SERVICE_URL;

    public CartGatewayController() {
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    @PostMapping("/items")
    public ResponseEntity<String> addToCart(@RequestBody(required = false) String body,
                                            HttpServletRequest request) {
        return forwardRequest(CART_SERVICE_URL + "/items", HttpMethod.POST, request, body);
    }

    @GetMapping
    public ResponseEntity<String> getCart(HttpServletRequest request) {
        return forwardRequest(CART_SERVICE_URL, HttpMethod.GET, request, null);
    }

    @DeleteMapping("/items")
    public ResponseEntity<String> deleteItem(HttpServletRequest request) {
        return forwardRequest(CART_SERVICE_URL + "/items", HttpMethod.DELETE, request, null);
    }

    private ResponseEntity<String> forwardRequest(String url,
                                                  HttpMethod method,
                                                  HttpServletRequest request,
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


