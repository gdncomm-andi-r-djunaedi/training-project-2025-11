package com.gdn.marketplace.gateway.filter;

import com.gdn.marketplace.gateway.config.RouterValidator;
import com.gdn.marketplace.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    private AuthenticationFilter authenticationFilter;
    private RouterValidator routerValidator;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        routerValidator = new RouterValidator();
        authenticationFilter = new AuthenticationFilter();
        ReflectionTestUtils.setField(authenticationFilter, "routerValidator", routerValidator);
        ReflectionTestUtils.setField(authenticationFilter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(authenticationFilter, "redisTemplate", redisTemplate);

        when(exchange.getRequest()).thenReturn(request);

    }

    @Test
    void filter_SecuredEndpoint_MissingToken_ReturnsUnauthorized() {
        when(request.getURI()).thenReturn(URI.create("/api/cart"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.setStatusCode(HttpStatus.UNAUTHORIZED)).thenReturn(true);
        when(response.setComplete()).thenReturn(Mono.empty());
        when(exchange.getResponse()).thenReturn(response);

        Mono<Void> result = authenticationFilter.apply(new AuthenticationFilter.Config()).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_OpenEndpoint_Success() {
        when(request.getURI()).thenReturn(URI.create("/api/member/login"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = authenticationFilter.apply(new AuthenticationFilter.Config()).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }
}
