package com.blublu.api_gateway.config;

import com.blublu.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

  private final JwtUtil jwtUtil;

  @Autowired
  public JwtAuthenticationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String token = getToken(exchange.getRequest());

    if (token != null && jwtUtil.isTokenValid(token, jwtUtil.extractUsername(token))) {
      String username = jwtUtil.extractUsername(token);
      Authentication auth = new UsernamePasswordAuthenticationToken(
          username, null, List.of());
      return chain.filter(exchange).contextWrite(
          ReactiveSecurityContextHolder.withAuthentication(auth));
    }

    return chain.filter(exchange);
  }

  private String getToken(ServerHttpRequest request) {
    String authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}
