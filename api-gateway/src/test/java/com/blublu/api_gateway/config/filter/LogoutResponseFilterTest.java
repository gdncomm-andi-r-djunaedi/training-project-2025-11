package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.interfaces.RedisService;
import com.blublu.api_gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogoutResponseFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisService redisService;

    @Mock
    private WebFilterChain filterChain;

    private LogoutResponseFilter logoutResponseFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logoutResponseFilter = new LogoutResponseFilter(jwtUtil, redisService);
    }

    @Test
    void testFilter_LogoutRequest_DeletesToken() {
        String token = "valid-token";
        String username = "testuser";

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/member/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.getTokenFromAuthHeaders(any())).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = logoutResponseFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(redisService).deleteRedisByKey(username);
        verify(filterChain).filter(exchange);
    }

    @Test
    void testFilter_NotLogoutRequest_DoesNothing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/other").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = logoutResponseFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(redisService, never()).deleteRedisByKey(anyString());
        verify(filterChain).filter(exchange);
    }
}
