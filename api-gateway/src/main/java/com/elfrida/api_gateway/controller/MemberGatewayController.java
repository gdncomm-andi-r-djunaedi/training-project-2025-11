package com.elfrida.api_gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/members")
public class MemberGatewayController {
    private final RestTemplate restTemplate;

    public MemberGatewayController() {
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

        this.restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response)
                    throws java.io.IOException {
                return false;
            }
        });
    }

    @Value("${member.service.url}")
    private String MEMBER_SERVICE_URL;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    MEMBER_SERVICE_URL + "/register", request, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    MEMBER_SERVICE_URL + "/login", request, String.class);

            return ResponseEntity.status(response.getStatusCode())
                    .body(response.getBody());
        } catch (Exception e) {
            System.out.println("Gateway Login Generic Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"message\":\"Gateway error: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(jakarta.servlet.http.HttpServletRequest servletRequest) {
        HttpHeaders headers = new HttpHeaders();
        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        }

        HttpEntity<String> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    MEMBER_SERVICE_URL + "/logout", request, String.class);

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }
}
