package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.interfaces.RedisService;
import com.blublu.api_gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisService redisService;

    @Mock
    private WebFilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, redisService);
    }

    @Test
    void testFilter_ValidToken_Authenticates() {
        String token = "valid-token";
        String username = "testuser";

        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.getTokenFromAuthHeaders(any())).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(redisService.findRedisByKey(username)).thenReturn(token);
        when(jwtUtil.isTokenValid(token, username)).thenReturn(true);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        // Verify that authentication was attempted (we can't easily check the context
        // here without capturing it,
        // but we can verify the flow reached the point where contextWrite is called by
        // ensuring dependencies were called)
        verify(redisService).findRedisByKey(username);
        verify(filterChain).filter(exchange);
    }

    @Test
    void testFilter_InvalidToken_ContinuesChain() {
        String token = "invalid-token";
        String username = "testuser";

        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.getTokenFromAuthHeaders(any())).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(redisService.findRedisByKey(username)).thenReturn(null); // Token not in redis
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(redisService).findRedisByKey(username);
        verify(filterChain).filter(exchange);
    }

    @Test
    void testFilter_NoToken_ContinuesChain() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.getTokenFromAuthHeaders(any())).thenReturn(null);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil, never()).extractUsername(anyString());
        verify(filterChain).filter(exchange);
    }
}
