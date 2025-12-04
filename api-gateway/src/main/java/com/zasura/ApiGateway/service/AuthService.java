package com.zasura.apiGateway.service;

import com.zasura.apiGateway.dto.CommonResponse;
import com.zasura.apiGateway.dto.LoginRequest;
import com.zasura.apiGateway.dto.VerifyMemberResponse;
import com.zasura.apiGateway.exception.AuthenticationFailedException;
import com.zasura.apiGateway.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final WebClient memberServiceWebClient;
  private final ReactiveAuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final CookieUtil cookieUtil;
  private final CacheService cacheService;

  public Mono<String> login(LoginRequest request) {
    ParameterizedTypeReference<CommonResponse<VerifyMemberResponse>> typeRef =
        new ParameterizedTypeReference<>() {
        };

    return memberServiceWebClient.post()
        .uri("/api/member/_verify")
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
          throw new AuthenticationFailedException("Authentication failed: Invalid credentials.");
        })
        .onStatus(HttpStatusCode::is5xxServerError,
            clientResponse -> Mono.error(new RuntimeException(
                "Member service is currently unavailable.")))
        .bodyToMono(typeRef)
        .map(responseEntity -> {
          String token = jwtService.generateToken(responseEntity.getData().getUid());
          cacheService.set(responseEntity.getData().getUid(),
              token,
              10,
              java.util.concurrent.TimeUnit.MINUTES);
          return token;
        });
  }

  public Mono<Boolean> logout(ServerWebExchange exchange) {
    String token = extractJwtFromRequest(exchange);
    if (token == null) {
      throw new AuthenticationFailedException("No User are login!");
    }
    return Mono.just(jwtService.extractClaim(token,
        claims -> cacheService.delete(claims.getSubject())));
  }

  private String extractJwtFromRequest(ServerWebExchange exchange) {
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}
