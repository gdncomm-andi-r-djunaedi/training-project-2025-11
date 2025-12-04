package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.interfaces.RedisService;
import com.blublu.api_gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
@Component
public class JwtAuthenticationFilter implements WebFilter {

  private final JwtUtil jwtUtil;

  @Autowired
  RedisService redisService;

  @Autowired
  public JwtAuthenticationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    log.info("Executing {}", JwtAuthenticationFilter.class);
    String token = jwtUtil.getTokenFromAuthHeaders(exchange.getRequest());

    if (token != null) {
      String username = jwtUtil.extractUsername(token);
      if (!username.isEmpty()) {
        log.info("Searching for redis key: {}", username);
        String tokenFromRedis = redisService.findRedisByKey(username);
        if (!Objects.isNull(tokenFromRedis) && token.equals(tokenFromRedis) && jwtUtil.isTokenValid(token,
            jwtUtil.extractUsername(tokenFromRedis))) {
          log.info("Token from redis found: {}. Authenticating user...", tokenFromRedis);
          Authentication auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
          return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        }
      }
    }

    return chain.filter(exchange);
  }
}
