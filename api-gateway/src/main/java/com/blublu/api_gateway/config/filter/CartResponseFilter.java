package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.interfaces.RedisService;
import com.blublu.api_gateway.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@Slf4j
@Component
public class CartResponseFilter implements WebFilter {

  private final JwtUtil jwtUtil;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  RedisService redisService;

  @Autowired
  public CartResponseFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    log.info("Start filter {}", CartResponseFilter.class);
    String token = jwtUtil.getTokenFromAuthHeaders(exchange.getRequest());
    if (verifyPathAndRequestValid(exchange) && !Objects.isNull(token)) {
      URI uri = exchange.getRequest().getURI();

      MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>(exchange.getRequest().getQueryParams());
      queryParams.remove("username");

      uri = UriComponentsBuilder.fromUri(uri)
          .replaceQueryParams(queryParams)
          .queryParam("username", jwtUtil.extractUsername(token))
          .build(true)
          .toUri();

      // Create modified request
      ServerHttpRequest modifiedRequest = exchange.getRequest().mutate().uri(uri).build();
      return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    return chain.filter(exchange);
  }

  private boolean verifyPathAndRequestValid(ServerWebExchange exchange) {
    return exchange.getRequest().getURI().getPath().contains("cart");
  }
}
