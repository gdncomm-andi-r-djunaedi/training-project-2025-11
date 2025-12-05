package com.ecommerce.gateway.controller;

import com.ecommerce.gateway.config.SecurityConfig;
import com.ecommerce.gateway.service.JwtService;
import com.ecommerce.gateway.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

        @Autowired
        private WebTestClient webTestClient;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private RestTemplate restTemplate;

        @MockBean
        private TokenBlacklistService tokenBlacklistService;

        @MockBean
        private ReactiveJwtDecoder reactiveJwtDecoder;

        @Test
        public void login_ShouldReturnToken() {
                // Mock RestTemplate response from Member Service
                Map<String, Object> memberResponse = Map.of(
                                "id", 1,
                                "username", "testuser");
                when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                                .thenReturn(memberResponse);

                // Mock JwtService
                when(jwtService.generateToken("testuser", 1L)).thenReturn("mock-token");

                webTestClient.post()
                                .uri("/gateway/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("username", "testuser", "password", "password"))
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.token").isEqualTo("mock-token")
                                .jsonPath("$.userId").isEqualTo(1)
                                .jsonPath("$.username").isEqualTo("testuser");
        }
}
