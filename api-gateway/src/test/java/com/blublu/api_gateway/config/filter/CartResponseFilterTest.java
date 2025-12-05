package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartResponseFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private WebFilterChain filterChain;

    private CartResponseFilter cartResponseFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cartResponseFilter = new CartResponseFilter(jwtUtil);
    }

    @Test
    void testFilter_CartRequest_AddsUsernameQueryParam() {
        String token = "valid-token";
        String username = "testuser";

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart/items")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.getTokenFromAuthHeaders(any())).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = cartResponseFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(captor.capture());

        ServerWebExchange capturedExchange = captor.getValue();
        String query = capturedExchange.getRequest().getURI().getQuery();
        assertTrue(query.contains("username=" + username));
    }

    @Test
    void testFilter_NullAuthorizationToken() {
      MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart/items")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + null)
          .queryParam("username", "username")
          .build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);
      when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

      Mono<Void> result = cartResponseFilter.filter(exchange, filterChain);
      StepVerifier.create(result)
          .verifyComplete();

      ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
      verify(filterChain).filter(captor.capture());

      ServerWebExchange capturedExchange = captor.getValue();
      String query = capturedExchange.getRequest().getURI().getQuery();
      assertTrue(query.contains("username=" + "username"));
    }

    @Test
    void testFilter_NotCartRequest_DoesNothing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/other").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.getTokenFromAuthHeaders(any())).thenReturn(null);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = cartResponseFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }
}
