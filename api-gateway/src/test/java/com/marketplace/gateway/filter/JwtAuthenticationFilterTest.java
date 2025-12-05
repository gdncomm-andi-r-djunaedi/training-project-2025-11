package com.marketplace.gateway.filter;

import com.marketplace.common.util.JwtUtil;
import com.marketplace.gateway.exception.InvalidTokenException;
import com.marketplace.gateway.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private GatewayFilterChain chain;

    private GatewayFilter gatewayFilter;

    @BeforeEach
    void setUp() {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, cookieUtil);
        gatewayFilter = jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config());
    }

    @Test
    void apply_ValidTokenInHeader_AllowsRequest() {
        String validToken = "valid.jwt.token";
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).extractUserId(validToken);
        verify(jwtUtil).extractEmail(validToken);
        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void apply_ValidTokenInCookie_AllowsRequest() {
        String validToken = "valid.jwt.token";
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String cookieName = "auth_token";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .cookie(new HttpCookie(cookieName, validToken))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(cookieName);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtUtil).validateToken(validToken);
    }

    @Test
    void apply_CookieTakesPrecedenceOverHeader() {
        String cookieToken = "cookie.jwt.token";
        String headerToken = "header.jwt.token";
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String cookieName = "auth_token";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
                .cookie(new HttpCookie(cookieName, cookieToken))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(cookieName);
        when(jwtUtil.validateToken(cookieToken)).thenReturn(true);
        when(jwtUtil.extractUserId(cookieToken)).thenReturn(userId);
        when(jwtUtil.extractEmail(cookieToken)).thenReturn(email);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtUtil).validateToken(cookieToken); // Should use cookie token
        verify(jwtUtil, never()).validateToken(headerToken); // Should not use header token
    }

    @Test
    void apply_MissingToken_ThrowsException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn("auth_token");

        assertThrows(InvalidTokenException.class, () -> {
            gatewayFilter.filter(exchange, chain).block();
        });

        verify(chain, never()).filter(any());
    }

    @Test
    void apply_InvalidToken_ThrowsException() {
        String invalidToken = "invalid.token";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> {
            gatewayFilter.filter(exchange, chain).block();
        });

        verify(chain, never()).filter(any());
    }

    @Test
    void apply_ExpiredToken_ThrowsException() {
        String expiredToken = "expired.jwt.token";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(expiredToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        assertThrows(InvalidTokenException.class, () -> {
            gatewayFilter.filter(exchange, chain).block();
        });

        verify(chain, never()).filter(any());
    }

    @Test
    void apply_MalformedAuthorizationHeader_ThrowsException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "NotBearer token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn("auth_token");

        assertThrows(InvalidTokenException.class, () -> {
            gatewayFilter.filter(exchange, chain).block();
        });
    }

    @Test
    void apply_EmptyCookie_FallsBackToHeader() {
        String validToken = "valid.jwt.token";
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String cookieName = "auth_token";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .cookie(new HttpCookie(cookieName, "")) // Empty cookie
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(cookieUtil.getAuthCookieName()).thenReturn(cookieName);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtUtil).validateToken(validToken);
    }

    @Test
    void apply_ValidToken_AddsUserHeadersToRequest() {
        String validToken = "valid.jwt.token";
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange modifiedExchange = invocation.getArgument(0);
            // Verify headers were added
            assertEquals(userId.toString(),
                    modifiedExchange.getRequest().getHeaders().getFirst("X-User-Id"));
            assertEquals(email,
                    modifiedExchange.getRequest().getHeaders().getFirst("X-User-Email"));
            return Mono.empty();
        });

        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void apply_ValidationException_ThrowsInvalidTokenException() {
        String badToken = "malformed.token";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + badToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(badToken)).thenThrow(new RuntimeException("Malformed token"));

        assertThrows(InvalidTokenException.class, () -> {
            gatewayFilter.filter(exchange, chain).block();
        });
    }
}
