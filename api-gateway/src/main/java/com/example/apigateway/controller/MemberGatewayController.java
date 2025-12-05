package com.example.apigateway.controller;

import com.example.apigateway.common.ApiResponse;
import com.example.apigateway.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/member")
@Tag(name = "Member")
@SecurityRequirement(name = "Bearer Authentication")
public class MemberGatewayController {

    private final RestTemplate restTemplate;

    @Value("${member.service.base-url}")
    private String memberServiceUrl;

    public MemberGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/currentUser")
    @Operation(summary = "Get current user")
    public ResponseEntity<ApiResponse<?>> currentUser(
            @AuthenticationPrincipal UserPrincipal principal) {

        String userId = principal.getUserId();

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(200)
                .status("OK")
                .data(Map.of("userId", userId))
                .errors(null)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<?>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        URI uri = UriComponentsBuilder
                .fromUriString(memberServiceUrl)
                .path("/api/member/users")
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