package com.zasura.apiGateway.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zasura.apiGateway.service.CacheService;
import com.zasura.apiGateway.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class AuthorizationFilter implements WebFilter, Ordered {
  private final CacheService cacheService;
  private final JwtService jwtService;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String token = extractJwtFromRequest(exchange);
    if (token != null) {
      return jwtService.validateToken(token).flatMap(isValid -> {
        if (isValid) {
          Claims claims = jwtService.extractAllClaims(token);
          String username = claims.getSubject();
          String cacheToken = cacheService.get(username, new TypeReference<>() {
          });
          if (cacheToken == null || !cacheToken.equals(token)) {
            return this.onError(exchange, "Token already Invalid", HttpStatus.UNAUTHORIZED);
          }

          URI newUri = UriComponentsBuilder.fromUri(exchange.getRequest().getURI())
              .queryParam("userId", username)
              .build()
              .toUri();
          ServerWebExchange modifiedExchange =
              exchange.mutate().request(builder -> builder.uri(newUri)).build();

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(username,
                  null,
                  Collections.singleton(new SimpleGrantedAuthority("USER")));
          return chain.filter(modifiedExchange)
              .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } else {
          return this.onError(exchange, "Token are Expired", HttpStatus.UNAUTHORIZED);
        }
      });
    }
    return chain.filter(exchange);
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(httpStatus);
    byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = response.bufferFactory().wrap(bytes);
    return response.writeWith(Mono.just(buffer));
  }

  private String extractJwtFromRequest(ServerWebExchange exchange) {
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  @Override
  public int getOrder() {
    return -101;
  }
}
