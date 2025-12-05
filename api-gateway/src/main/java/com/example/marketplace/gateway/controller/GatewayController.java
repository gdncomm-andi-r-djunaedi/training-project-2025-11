package com.example.marketplace.gateway.controller;

import com.example.marketplace.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.member.url:http://localhost:8081}")
    private String memberBaseUrl;

    @Value("${services.product.url:http://localhost:8082}")
    private String productBaseUrl;

    @Value("${services.cart.url:http://localhost:8083}")
    private String cartBaseUrl;

    @GetMapping("/health")
    public String health() {
        return "api-gateway: ok";
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        return restTemplate.postForEntity(memberBaseUrl + "/internal/members/register", body, Object.class);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        return restTemplate.postForEntity(memberBaseUrl + "/internal/members/login", body, Object.class);
    }

    @GetMapping("/products")
    public ResponseEntity<?> listProducts(@RequestParam(value = "q", required = false) String q,
                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                          @RequestParam(value = "size", defaultValue = "20") int size) {
        String url = productBaseUrl + "/internal/products?q=" + (q == null ? "" : q) +
                "&page=" + page + "&size=" + size;
        return restTemplate.getForEntity(url, Object.class);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> productDetail(@PathVariable String id) {
        return restTemplate.getForEntity(productBaseUrl + "/internal/products/" + id, Object.class);
    }

    @PostMapping("/cart")
    public ResponseEntity<?> addToCart(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(cartBaseUrl + "/internal/cart", HttpMethod.POST, entity, Object.class);
    }

    @GetMapping("/cart")
    public ResponseEntity<?> getCart(@RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(cartBaseUrl + "/internal/cart", HttpMethod.GET, entity, Object.class);
    }
}
