package com.zasura.apiGateway.controller;

import com.zasura.apiGateway.dto.CommonResponse;
import com.zasura.apiGateway.dto.LoginRequest;
import com.zasura.apiGateway.dto.LoginResponse;
import com.zasura.apiGateway.service.AuthService;
import com.zasura.apiGateway.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final CookieUtil cookieUtil;
  private final AuthService authService;

  @PostMapping("/login")
  public Mono<ResponseEntity<CommonResponse<LoginResponse>>> login(@RequestBody LoginRequest request) {
    return authService.login(request)
        .map(token -> ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookieUtil.createAuthCookie(token))
            .body(CommonResponse.success(new LoginResponse(token))));
  }

  @DeleteMapping("/logout")
  public Mono<ResponseEntity<CommonResponse<Boolean>>> logout(ServerWebExchange exchange) {
    return authService.logout(exchange)
        .map(result -> ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookieUtil.deleteAuthCookie())
            .body(CommonResponse.success(result)));
  }
}
