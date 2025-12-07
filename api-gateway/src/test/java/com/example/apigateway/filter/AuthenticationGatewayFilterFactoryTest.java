package com.example.apigateway.filter;

import com.example.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationGatewayFilterFactoryTest {

    @Mock
    private RouteValidator routeValidator;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private AuthenticationGatewayFilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        // Since isSecured is a field, we might need a real instance or careful mocking
        // if it calls logic.
        // RouteValidator has a Predicate field 'isSecured'.
        // Let's assume we can mock the behavior of verify logic or field access if
        // accessible.
        // However, RouteValidator likely has the Predicate defined inline. We should
        // mock the Predicate itself if possible,
        // or just mock the validator behavior if the filter uses it as a bean.
        // Looking at source: routeValidator.isSecured.test(request).
        // It accesses a public field 'isSecured'. We need to make sure 'routeValidator'
        // isn't null and has this field.
        // Since we are mocking RouteValidator, the public field 'isSecured' will be
        // null by default unless we set it or use a real object for it.
        // It's safer to use a real RouteValidator if it has no dependencies, or set the
        // field.

        // Strategy: Use a real RouteValidator instance for the test to avoid NPE on
        // field access,
        // OR mock the field if it's not final. It is likely a functional interface.
        // Let's double check implementation of RouteValidator in next steps if tests
        // fail, but for now
        // I will assign a mock Predicate to the field if possible or just use a real
        // simple RouteValidator.
        // Actually best is to instantiate RouteValidator if it's a simple component.
    }

    @Test
    void apply_shouldAuthenticateAndMutateHeader_whenHeaderHasValidToken() {
        // Setup
        RouteValidator realValidator = new RouteValidator(); // Using real object as it logic-only
        filterFactory = new AuthenticationGatewayFilterFactory();
        org.springframework.test.util.ReflectionTestUtils.setField(filterFactory, "routeValidator", realValidator);
        org.springframework.test.util.ReflectionTestUtils.setField(filterFactory, "jwtUtil", jwtUtil);

        AuthenticationGatewayFilterFactory.Config config = new AuthenticationGatewayFilterFactory.Config();
        GatewayFilter filter = filterFactory.apply(config);

        String token = "valid-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header("Authorization", "Bearer " + token).build();
        MockServerHttpResponse response = new MockServerHttpResponse();
        ServerWebExchange exchange = Mockito.spy(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);

        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("123");
        when(jwtUtil.extractToken(any())).thenReturn(Optional.of(token));
        lenient().when(jwtUtil.validateToken(token)).thenReturn(claims);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Execute
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // Verify
        verify(jwtUtil).validateToken(token); // Ensure it was called
        verify(chain).filter(argThat(ex -> {
            String userId = ex.getRequest().getHeaders().getFirst("X-User-Id");
            return "123".equals(userId);
        }));
    }

    @Test
    void apply_shouldReturnUnauthorized_whenTokenIsInvalid() {
        RouteValidator realValidator = new RouteValidator();
        filterFactory = new AuthenticationGatewayFilterFactory();
        org.springframework.test.util.ReflectionTestUtils.setField(filterFactory, "routeValidator", realValidator);
        org.springframework.test.util.ReflectionTestUtils.setField(filterFactory, "jwtUtil", jwtUtil);

        GatewayFilter filter = filterFactory.apply(new AuthenticationGatewayFilterFactory.Config());

        String token = "invalid-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart")
                .header("Authorization", "Bearer " + token).build();
        MockServerHttpResponse response = new MockServerHttpResponse();
        ServerWebExchange exchange = Mockito.spy(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);

        when(jwtUtil.extractToken(any())).thenReturn(Optional.of(token));
        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Valid"));

        // Execute
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete(); // It completes, but writes 401 to response

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void apply_shouldSkipAuthentication_forOpenEndpoints() {
        RouteValidator realValidator = new RouteValidator();
        filterFactory = new AuthenticationGatewayFilterFactory();
        org.springframework.test.util.ReflectionTestUtils.setField(filterFactory, "routeValidator", realValidator);
        org.springframework.test.util.ReflectionTestUtils.setField(filterFactory, "jwtUtil", jwtUtil);

        GatewayFilter filter = filterFactory.apply(new AuthenticationGatewayFilterFactory.Config());

        // Open endpoint
        MockServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();
        MockServerHttpResponse response = new MockServerHttpResponse();
        ServerWebExchange exchange = Mockito.spy(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtUtil, never()).extractToken(any());
        verify(chain).filter(exchange);
    }

    @Test
    void apply_shouldReturnUnauthorized_whenTokenIsMissing() {
        RouteValidator realValidator = new RouteValidator();
        filterFactory = new AuthenticationGatewayFilterFactory();
        org.springframework.test.util.ReflectionTestUtils.setField(filterFactory, "routeValidator", realValidator);
        org.springframework.test.util.ReflectionTestUtils.setField(filterFactory, "jwtUtil", jwtUtil);

        GatewayFilter filter = filterFactory.apply(new AuthenticationGatewayFilterFactory.Config());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart").build(); // No Authorization header
        MockServerHttpResponse response = new MockServerHttpResponse();
        ServerWebExchange exchange = Mockito.spy(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);

        when(jwtUtil.extractToken(any())).thenReturn(Optional.empty());

        // Execute
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
