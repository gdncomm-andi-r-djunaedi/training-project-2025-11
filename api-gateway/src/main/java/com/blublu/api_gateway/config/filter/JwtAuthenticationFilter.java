package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.interfaces.RedisService;
import com.blublu.api_gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

  private final JwtUtil jwtUtil;
  private final RedisService redisService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, RedisService redisService) {
    this.jwtUtil = jwtUtil;
    this.redisService = redisService;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    log.info("Start filter {}", JwtAuthenticationFilter.class);
    String token = jwtUtil.getTokenFromAuthHeaders(exchange.getRequest());

    if (token != null) {
      String username = jwtUtil.extractUsername(token);
      if (!username.isEmpty()) {
        String tokenFromRedis = redisService.findRedisByKey(username);
        if (!Objects.isNull(tokenFromRedis) && token.equals(tokenFromRedis) && jwtUtil.isTokenValid(token,
            jwtUtil.extractUsername(tokenFromRedis))) {
          log.info("Authenticating user {} with token {}", username, tokenFromRedis);
          Authentication auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
          return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        }
      }
    }

    return chain.filter(exchange);
  }
}
