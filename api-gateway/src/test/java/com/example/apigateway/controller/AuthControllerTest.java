package com.example.apigateway.controller;

import com.example.apigateway.client.MemberClient;
import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.LoginResponse;
import com.example.apigateway.dto.MemberValidationResponse;
import com.example.apigateway.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MemberClient memberClient;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void login_shouldReturnTokenAndSetCookie_whenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest("user", "password");
        MemberValidationResponse memberResponse = new MemberValidationResponse(1L, "user");
        String token = "generated-token";
        ResponseCookie cookie = ResponseCookie.from("jwt", token).build();

        when(memberClient.validateCredentials(any(LoginRequest.class))).thenReturn(Mono.just(memberResponse));
        when(jwtUtil.createToken(anyString())).thenReturn(token);
        when(jwtUtil.createCookie(anyString())).thenReturn(cookie);

        webTestClient.post()
                .uri("/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .consumeWith(response -> {
                    LoginResponse body = response.getResponseBody();
                    org.junit.jupiter.api.Assertions.assertNotNull(body);
                    org.junit.jupiter.api.Assertions.assertEquals(token, body.getToken());
                });
    }

    @Test
    void logout_shouldClearCookie() {
        ResponseCookie emptyCookie = ResponseCookie.from("jwt", "").maxAge(0).build();
        when(jwtUtil.clearCookie()).thenReturn(emptyCookie);

        webTestClient.post()
                .uri("/auth/logout")
                .exchange()
                .expectStatus().isOk()
                .expectCookie().value("jwt", value -> org.junit.jupiter.api.Assertions.assertTrue(value.isEmpty()))
                .expectCookie().maxAge("jwt", java.time.Duration.ofSeconds(0));
    }
}
