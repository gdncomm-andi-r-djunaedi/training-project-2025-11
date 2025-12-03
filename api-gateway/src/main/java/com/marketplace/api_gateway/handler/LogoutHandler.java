package com.marketplace.api_gateway.handler;

import com.marketplace.api_gateway.model.ApiResponse;
import com.marketplace.api_gateway.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LogoutHandler {
  private final JwtUtils jwtUtils;

  public Mono<ServerResponse> handleLogout(ServerRequest request) {
    String token = jwtUtils.extractToken(request);

    if (token == null) {
      ApiResponse<Object> res =
          ApiResponse.builder().success(false).message("Missing token").build();

      return ServerResponse.badRequest().bodyValue(res);
    }

    if (!jwtUtils.validateToken(token)) {
      ApiResponse<Object> res =
          ApiResponse.builder().success(false).message("Invalid or expired token").build();

      return ServerResponse.status(401).bodyValue(res);
    }

    String username = jwtUtils.extractUsername(token);

    ResponseCookie clearCookie = ResponseCookie.from("token", "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .build();

    ApiResponse<Object> res = ApiResponse.builder()
        .success(true)
        .message("Logged out successfully for user: " + username)
        .build();

    return ServerResponse.ok().cookie(clearCookie).bodyValue(res);
  }
}
