package com.marketplace.gateway.filter;

import com.marketplace.common.util.JwtUtil;
import com.marketplace.gateway.service.TokenBlacklistService;
import com.marketplace.gateway.util.CookieUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private GatewayFilterChain chain;

    private AuthFilter authFilter;

    private static final String AUTH_COOKIE_NAME = "auth_token";

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter(jwtUtil, cookieUtil, tokenBlacklistService);
    }

    @Test
    void filter_PublicPath_Register_SkipsAuth() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/member/register")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        verify(chain).filter(exchange);
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void filter_PublicPath_Login_SkipsAuth() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        verify(chain).filter(exchange);
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    @Test
    void filter_PublicPath_Product_SkipsAuth() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/search")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        verify(chain).filter(exchange);
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    @Test
    void filter_PublicPath_ProductDetail_SkipsAuth() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_ProtectedPath_NoToken_Returns401() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_ProtectedPath_ValidToken_AllowsRequest() {
        // Arrange
        String validToken = "valid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(Mono.just(false));
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        verify(tokenBlacklistService).isBlacklisted(validToken);
        verify(jwtUtil).validateToken(validToken);
        verify(chain).filter(exchange);
    }

    @Test
    void filter_ProtectedPath_BlacklistedToken_Returns401() {
        // Arrange
        String blacklistedToken = "blacklisted.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + blacklistedToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(tokenBlacklistService.isBlacklisted(blacklistedToken)).thenReturn(Mono.just(true));

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(tokenBlacklistService).isBlacklisted(blacklistedToken);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_ProtectedPath_InvalidToken_Returns401() {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(tokenBlacklistService.isBlacklisted(invalidToken)).thenReturn(Mono.just(false));
        when(jwtUtil.validateToken(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_TokenFromCookie_TakesPrecedence() {
        // Arrange
        String cookieToken = "cookie.jwt.token";
        String headerToken = "header.jwt.token";
        
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .cookie(new HttpCookie(AUTH_COOKIE_NAME, cookieToken))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(tokenBlacklistService.isBlacklisted(cookieToken)).thenReturn(Mono.just(false));
        when(jwtUtil.validateToken(cookieToken)).thenReturn(true);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        // Should use cookie token, not header token
        verify(tokenBlacklistService).isBlacklisted(cookieToken);
        verify(tokenBlacklistService, never()).isBlacklisted(headerToken);
        verify(jwtUtil).validateToken(cookieToken);
        verify(jwtUtil, never()).validateToken(headerToken);
        verify(chain).filter(exchange);
    }

    @Test
    void filter_EmptyCookie_FallsBackToHeader() {
        // Arrange
        String headerToken = "header.jwt.token";
        
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .cookie(new HttpCookie(AUTH_COOKIE_NAME, ""))  // Empty cookie
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(AUTH_COOKIE_NAME);
        when(tokenBlacklistService.isBlacklisted(headerToken)).thenReturn(Mono.just(false));
        when(jwtUtil.validateToken(headerToken)).thenReturn(true);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();

        // Should fall back to header token
        verify(tokenBlacklistService).isBlacklisted(headerToken);
        verify(jwtUtil).validateToken(headerToken);
        verify(chain).filter(exchange);
    }

    @Test
    void filter_GetOrder_ReturnsMinusOne() {
        // The filter should have high priority (low order number)
        assertEquals(-1, authFilter.getOrder());
    }
}

