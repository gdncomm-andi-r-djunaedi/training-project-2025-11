package com.gdn.faurihakim.gateway.filter;

import com.gdn.faurihakim.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationFilter Security Tests")
class AuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpRequest.Builder requestBuilder;

    @Mock
    private ServerHttpResponse response;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";
    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
        lenient().when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should allow /api/auth requests without token")
    void testFilter_AuthEndpoint_AllowedWithoutToken() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/auth/login"));
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should allow POST /api/members (registration) without token")
    void testFilter_MemberRegistration_AllowedWithoutToken() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/members"));
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should allow GET /api/members/{memberId} without token")
    void testFilter_GetMemberById_AllowedWithoutToken() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/members/123"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should reject PUT /api/members without valid token")
    void testFilter_UpdateMember_RequiresToken() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/members"));
        when(request.getMethod()).thenReturn(HttpMethod.PUT);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.setComplete()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should allow request with valid JWT token and add X-User-Id header")
    void testFilter_ValidToken_AddsUserIdHeaderAndProceeds() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/carts/items"));
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN);
        when(request.getHeaders()).thenReturn(headers);

        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(USER_ID);

        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header("X-User-Id", USER_ID)).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(mock(ServerWebExchange.Builder.class));
        when(exchange.mutate().request(request)).thenReturn(mock(ServerWebExchange.Builder.class));
        when(exchange.mutate().request(request).build()).thenReturn(exchange);

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(jwtUtil).validateToken(VALID_TOKEN);
        verify(jwtUtil).extractUserId(VALID_TOKEN);
        verify(requestBuilder).header("X-User-Id", USER_ID);
        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("Should reject request with invalid JWT token - returns 401")
    void testFilter_InvalidToken_Returns401() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/carts/items"));
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN);
        when(request.getHeaders()).thenReturn(headers);

        when(jwtUtil.validateToken(INVALID_TOKEN)).thenReturn(false);
        when(response.setComplete()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(jwtUtil).validateToken(INVALID_TOKEN);
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should reject request with missing Authorization header - returns 401")
    void testFilter_MissingAuthHeader_Returns401() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/carts/items"));
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.setComplete()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(jwtUtil, never()).validateToken(any());
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should reject request with malformed Authorization header (no Bearer prefix) - returns 401")
    void testFilter_MalformedAuthHeader_Returns401() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/carts/items"));
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "InvalidFormat " + VALID_TOKEN);
        when(request.getHeaders()).thenReturn(headers);
        when(response.setComplete()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(jwtUtil, never()).validateToken(any());
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should reject request with empty Bearer token - returns 401")
    void testFilter_EmptyBearerToken_Returns401() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/carts/items"));
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer ");
        when(request.getHeaders()).thenReturn(headers);
        when(response.setComplete()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should have high priority order (-1)")
    void testGetOrder_ReturnsHighPriority() {
        // Act
        int order = authenticationFilter.getOrder();

        // Assert
        assertThat(order).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should protect /api/products endpoint with token")
    void testFilter_ProductEndpoint_RequiresToken() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/products"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.setComplete()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should protect /api/carts endpoint with token")
    void testFilter_CartEndpoint_RequiresToken() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/carts"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.setComplete()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should handle case-sensitive Bearer prefix correctly")
    void testFilter_CaseSensitiveBearer_Returns401() {
        // Arrange
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/carts/items"));
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "bearer " + VALID_TOKEN); // lowercase
        when(request.getHeaders()).thenReturn(headers);
        when(response.setComplete()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}
