package com.dev.onlineMarketplace.gateway.filter;

import com.dev.onlineMarketplace.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    private AuthenticationFilter authenticationFilter;
    private JwtUtil jwtUtil;
    private RouteValidator routeValidator;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        routeValidator = mock(RouteValidator.class);
        authenticationFilter = new AuthenticationFilter(jwtUtil, routeValidator);

        // Mock RouteValidator to simulate secured route
        routeValidator.isSecured = request -> true;
    }

    @Test
    void testApply_MissingAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void testApply_ValidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        when(jwtUtil.validateToken("validToken")).thenReturn(true);

        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() != HttpStatus.UNAUTHORIZED;
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testApply_InvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalidToken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(jwtUtil.validateToken("invalidToken")).thenThrow(new RuntimeException("Invalid token"));

        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }
}
