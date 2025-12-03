package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.interfaces.RedisService;
import com.blublu.api_gateway.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LogoutResponseFilter implements WebFilter {

  private final JwtUtil jwtUtil;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  RedisService redisService;

  @Autowired
  public LogoutResponseFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (verifyPathAndRequestValid(exchange)) {
      log.info("removing redis key: {}", exchange.getRequest().getQueryParams().getFirst("username"));
      redisService.deleteRedisByKey(jwtUtil.extractUsername(jwtUtil.getTokenFromAuthHeaders(exchange.getRequest())));
    }
    return chain.filter(exchange);
  }

  private boolean verifyPathAndRequestValid(ServerWebExchange exchange) {
    return exchange.getRequest().getURI().getPath().contains("member/logout") && exchange.getRequest()
        .getMethod()
        .equals(HttpMethod.POST);
  }
}
