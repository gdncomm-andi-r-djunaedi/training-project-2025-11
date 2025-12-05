package com.example.gateway.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@RestController
public class GatewayController {

    private final RestTemplate rest = new RestTemplate();

    @Value("${member.service.url:http://localhost:8081}")
    private String memberUrl;

    @Value("${product.service.url:http://localhost:8082}")
    private String productUrl;

    @Value("${cart.service.url:http://localhost:8083}")
    private String cartUrl;

    // Health proxy
    @GetMapping("/api/health/{service}")
    public ResponseEntity<String> proxyHealth(@PathVariable String service) {
        String url = switch(service) {
            case "member" -> memberUrl + "/api/health";
            case "product" -> productUrl + "/api/health";
            case "cart" -> cartUrl + "/api/health";
            default -> null;
        };
        if (url == null) return ResponseEntity.badRequest().body("Unknown service");
        String resp = rest.getForObject(url, String.class);
        return ResponseEntity.ok(resp);
    }
}
