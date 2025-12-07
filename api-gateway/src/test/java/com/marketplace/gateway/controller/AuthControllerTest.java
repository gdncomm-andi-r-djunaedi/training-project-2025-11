package com.marketplace.gateway.controller;

import com.marketplace.common.command.ReactiveCommandExecutor;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.gateway.command.LoginCommand;
import com.marketplace.gateway.dto.LoginRequest;
import com.marketplace.gateway.dto.LoginResponse;
import com.marketplace.gateway.service.TokenBlacklistService;
import com.marketplace.gateway.util.CookieUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private ReactiveCommandExecutor commandExecutor;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private AuthController authController;

    private static final String AUTH_COOKIE_NAME = "auth_token";
    private static final Long JWT_EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        authController = new AuthController(cookieUtil, commandExecutor, tokenBlacklistService);
        // Set the @Value field via reflection since we're not using Spring context
        ReflectionTestUtils.setField(authController, "jwtExpiration", JWT_EXPIRATION);
    }

    @Test
    void login_ValidCredentials_ReturnsOkWithTokenAndCookie() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("Password123!")
                .build();

        UUID userId = UUID.randomUUID();
        String token = "valid.jwt.token";
        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .id(userId)
                .email("user@example.com")
                .build();

        ResponseCookie expectedCookie = ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ofMillis(86400000))
                .path("/")
                .build();

        MockServerHttpResponse response = new MockServerHttpResponse();

        when(commandExecutor.execute(eq(LoginCommand.class), any(LoginRequest.class)))
                .thenReturn(Mono.just(loginResponse));
        when(cookieUtil.createAuthCookie(eq(token), anyLong()))
                .thenReturn(expectedCookie);

        // Act
        Mono<ResponseEntity<ApiResponse<LoginResponse>>> result = authController.login(request, response);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.OK, entity.getStatusCode());
                    assertNotNull(entity.getBody());
                    assertTrue(entity.getBody().isSuccess());
                    assertEquals("Login successful", entity.getBody().getMessage());
                    assertEquals(token, entity.getBody().getData().getToken());
                    assertEquals("user@example.com", entity.getBody().getData().getEmail());
                })
                .verifyComplete();

        verify(commandExecutor).execute(eq(LoginCommand.class), any(LoginRequest.class));
        verify(cookieUtil).createAuthCookie(eq(token), anyLong());
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("wrongPassword")
                .build();

        MockServerHttpResponse response = new MockServerHttpResponse();

        when(commandExecutor.execute(eq(LoginCommand.class), any(LoginRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Invalid credentials")));

        // Act
        Mono<ResponseEntity<ApiResponse<LoginResponse>>> result = authController.login(request, response);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
                    assertNotNull(entity.getBody());
                    assertFalse(entity.getBody().isSuccess());
                    assertEquals("Invalid credentials", entity.getBody().getMessage());
                })
                .verifyComplete();

        verify(commandExecutor).execute(eq(LoginCommand.class), any(LoginRequest.class));
        verify(cookieUtil, never()).createAuthCookie(anyString(), anyLong());
    }

    @Test
    void logout_WithTokenInCookie_BlacklistsTokenAndInvalidatesCookie() {
        // Arrange
        String token = "valid.jwt.token";

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/logout")
                .cookie(new HttpCookie(AUTH_COOKIE_NAME, token))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseCookie logoutCookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .path("/")
                .build();

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(tokenBlacklistService.blacklistToken(token)).thenReturn(Mono.just(true));
        when(cookieUtil.createLogoutCookie()).thenReturn(logoutCookie);

        // Act
        Mono<ResponseEntity<ApiResponse<Void>>> result = authController.logout(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.OK, entity.getStatusCode());
                    assertNotNull(entity.getBody());
                    assertTrue(entity.getBody().isSuccess());
                    assertEquals("Logout successful", entity.getBody().getMessage());
                })
                .verifyComplete();

        verify(tokenBlacklistService).blacklistToken(token);
        verify(cookieUtil).createLogoutCookie();
    }

    @Test
    void logout_WithTokenInHeader_BlacklistsToken() {
        // Arrange
        String token = "valid.jwt.token";

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseCookie logoutCookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .path("/")
                .build();

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(tokenBlacklistService.blacklistToken(token)).thenReturn(Mono.just(true));
        when(cookieUtil.createLogoutCookie()).thenReturn(logoutCookie);

        // Act
        Mono<ResponseEntity<ApiResponse<Void>>> result = authController.logout(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.OK, entity.getStatusCode());
                    assertNotNull(entity.getBody());
                    assertTrue(entity.getBody().isSuccess());
                    assertEquals("Logout successful", entity.getBody().getMessage());
                })
                .verifyComplete();

        verify(tokenBlacklistService).blacklistToken(token);
        verify(cookieUtil).createLogoutCookie();
    }

    @Test
    void logout_NoToken_StillReturnsSuccess() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/logout")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseCookie logoutCookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .path("/")
                .build();

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(cookieUtil.createLogoutCookie()).thenReturn(logoutCookie);

        // Act
        Mono<ResponseEntity<ApiResponse<Void>>> result = authController.logout(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.OK, entity.getStatusCode());
                    assertNotNull(entity.getBody());
                    assertTrue(entity.getBody().isSuccess());
                    assertEquals("Logout successful", entity.getBody().getMessage());
                })
                .verifyComplete();

        // Should not attempt to blacklist when no token present
        verify(tokenBlacklistService, never()).blacklistToken(anyString());
        verify(cookieUtil).createLogoutCookie();
    }

    @Test
    void logout_CookieTakesPrecedenceOverHeader() {
        // Arrange
        String cookieToken = "cookie.jwt.token";
        String headerToken = "header.jwt.token";

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/logout")
                .cookie(new HttpCookie(AUTH_COOKIE_NAME, cookieToken))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseCookie logoutCookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .path("/")
                .build();

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(tokenBlacklistService.blacklistToken(cookieToken)).thenReturn(Mono.just(true));
        when(cookieUtil.createLogoutCookie()).thenReturn(logoutCookie);

        // Act
        Mono<ResponseEntity<ApiResponse<Void>>> result = authController.logout(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.OK, entity.getStatusCode());
                    assertTrue(entity.getBody().isSuccess());
                })
                .verifyComplete();

        // Should blacklist cookie token, not header token
        verify(tokenBlacklistService).blacklistToken(cookieToken);
        verify(tokenBlacklistService, never()).blacklistToken(headerToken);
    }
}

