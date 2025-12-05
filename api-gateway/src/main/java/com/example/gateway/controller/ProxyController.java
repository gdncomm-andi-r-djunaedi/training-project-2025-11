package com.example.gateway.controller;

import com.example.commandlib.CommandExecutor;
import com.example.gateway.command.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PreDestroy;

@RestController
public class ProxyController {
    private final RestTemplate rest = new RestTemplate();
    private final CommandExecutor commandExecutor = new CommandExecutor();
    
    @Value("${member.url:http://localhost:8081}") private String memberUrl;
    @Value("${product.url:http://localhost:8082}") private String productUrl;
    @Value("${cart.url:http://localhost:8083}") private String cartUrl;

    @PreDestroy
    public void shutdown() {
        commandExecutor.shutdown();
    }

    @PostMapping("/auth/{path}")
    public ResponseEntity<?> proxyAuth(@PathVariable String path, @RequestBody(required = false) Object body) {
        String url = memberUrl + "/auth/" + path;
        return commandExecutor.execute(new ProxyPostCommand(rest, url, body));
    }

    @GetMapping("/products/search")
    public ResponseEntity<?> productsSearch(@RequestParam String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        String url = productUrl + "/products/search?q=" + q + "&page=" + page + "&size=" + size;
        return commandExecutor.execute(new ProxyGetCommand(rest, url));
    }

    @GetMapping("/products")
    public ResponseEntity<?> productsList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        String url = productUrl + "/products?page=" + page + "&size=" + size;
        return commandExecutor.execute(new ProxyGetCommand(rest, url));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> productDetail(@PathVariable String id) {
        String url = productUrl + "/products/" + id;
        return commandExecutor.execute(new ProxyGetCommand(rest, url));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<?> cartAdd(@RequestBody Object body, @RequestHeader(value = "Authorization", required = false) String auth, @RequestHeader(value = "Cookie", required = false) String cookie) {
        String url = cartUrl + "/cart/add";
        HttpHeaders headers = buildAuthHeaders(auth, cookie);
        return commandExecutor.execute(new ProxyPostCommand(rest, url, body, headers));
    }

    @GetMapping("/cart")
    public ResponseEntity<?> cartView(@RequestHeader(value = "Authorization", required = false) String auth, @RequestHeader(value = "Cookie", required = false) String cookie) {
        String url = cartUrl + "/cart";
        HttpHeaders headers = buildAuthHeaders(auth, cookie);
        return commandExecutor.execute(new ProxyGetCommand(rest, url, headers));
    }

    @DeleteMapping("/cart/remove/{sku}")
    public ResponseEntity<?> cartRemove(@PathVariable String sku, @RequestHeader(value = "Authorization", required = false) String auth, @RequestHeader(value = "Cookie", required = false) String cookie) {
        String url = cartUrl + "/cart/remove/" + sku;
        HttpHeaders headers = buildAuthHeaders(auth, cookie);
        return commandExecutor.execute(new ProxyDeleteCommand(rest, url, headers));
    }

    private HttpHeaders buildAuthHeaders(String auth, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        if (auth != null) {
            headers.set(HttpHeaders.AUTHORIZATION, auth);
        } else if (cookie != null) {
            for (String c : cookie.split(";")) {
                String t = c.trim();
                if (t.startsWith("JWT-TOKEN=")) {
                    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + t.substring("JWT-TOKEN=".length()));
                    break;
                }
            }
        }
        return headers;
    }
}
