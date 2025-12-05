package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.interfaces.RedisService;
import com.blublu.api_gateway.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginResponseFilterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Mock
  private JwtUtil jwtUtil;
  @Mock
  private RedisService redisService;
  @Mock
  private WebFilterChain filterChain;
  private LoginResponseFilter loginResponseFilter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    loginResponseFilter = new LoginResponseFilter(jwtUtil, redisService);
  }

  @Test
  void testFilter_LoginRequest_AddsToken() throws Exception {
    String username = "testuser";
    String token = "mocked-jwt-token";

    MockServerHttpRequest request = MockServerHttpRequest.post("/api/member/login").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(jwtUtil.generateToken(username)).thenReturn(token);

    // Mock the filter chain to write a response body
    when(filterChain.filter(any())).thenAnswer(invocation -> {
      ServerWebExchange ex = invocation.getArgument(0);
      ex.getResponse().setStatusCode(org.springframework.http.HttpStatus.OK);
      Map<String, String> responseBody = new HashMap<>();
      responseBody.put("username", username);
      byte[] bytes = objectMapper.writeValueAsBytes(responseBody);
      DataBuffer buffer = new DefaultDataBufferFactory().wrap(bytes);
      return ex.getResponse().writeWith(Flux.just(buffer));
    });

    Mono<Void> result = loginResponseFilter.filter(exchange, filterChain);

    StepVerifier.create(result).verifyComplete();

    // Verify response body contains token
    StepVerifier.create(exchange.getResponse().getBody()).assertNext(dataBuffer -> {
      byte[] bytes = new byte[dataBuffer.readableByteCount()];
      dataBuffer.read(bytes);
      String content = new String(bytes, StandardCharsets.UTF_8);
      assertTrue(content.contains(token));
      assertTrue(content.contains(username));
    }).verifyComplete();

    verify(redisService).createRedisCache(username, token);
  }

  @Test
  void testFilter_NotLoginRequest_NoToken() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/other").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(filterChain.filter(exchange)).thenReturn(Mono.empty());

    Mono<Void> result = loginResponseFilter.filter(exchange, filterChain);

    StepVerifier.create(result).verifyComplete();

    verify(jwtUtil, never()).generateToken(anyString());
    verify(filterChain).filter(exchange);
  }

  @Test
  void testFilter_NotLoginRequest_WrongMethod() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/member/login").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(filterChain.filter(exchange)).thenReturn(Mono.empty());

    Mono<Void> result = loginResponseFilter.filter(exchange, filterChain);

    StepVerifier.create(result).verifyComplete();

    verify(jwtUtil, never()).generateToken(anyString());
    verify(filterChain).filter(exchange);
  }

  @Test
  void testFilter_LoginRequest_BodyNotFlux_NoModification() throws Exception {
    String username = "testuser";
    String token = "mocked-jwt-token";

    MockServerHttpRequest request = MockServerHttpRequest.post("/api/member/login").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(jwtUtil.generateToken(anyString())).thenReturn(token);

    when(filterChain.filter(any())).thenAnswer(invocation -> {
      ServerWebExchange ex = invocation.getArgument(0);
      ex.getResponse().setStatusCode(org.springframework.http.HttpStatus.OK);
      Map<String, String> responseBody = new HashMap<>();
      responseBody.put("username", username);
      byte[] bytes = objectMapper.writeValueAsBytes(responseBody);
      DataBuffer buffer = new DefaultDataBufferFactory().wrap(bytes);
      // use Mono (not Flux) to exercise the branch where body is not instanceof Flux
      return ex.getResponse().writeWith(Mono.just(buffer));
    });

    Mono<Void> result = loginResponseFilter.filter(exchange, filterChain);

    StepVerifier.create(result).verifyComplete();

    StepVerifier.create(exchange.getResponse().getBody()).assertNext(dataBuffer -> {
      byte[] bytes = new byte[dataBuffer.readableByteCount()];
      dataBuffer.read(bytes);
      String content = new String(bytes, StandardCharsets.UTF_8);
      assertTrue(content.contains(username));
      assertFalse(content.contains("token"));
    }).verifyComplete();

    verify(redisService, never()).createRedisCache(anyString(), anyString());
    verify(jwtUtil, never()).generateToken(anyString());

  }
}
