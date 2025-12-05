package com.zasura.apiGateway.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
  private static final String COOKIE_NAME = "ACCESS_TOKEN";
  private static final int MAX_AGE_SECONDS = 10 * 60; // 10 minutes

  public String createAuthCookie(String token) {
    return ResponseCookie.from(COOKIE_NAME, token)
        .secure(true)
        .httpOnly(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(MAX_AGE_SECONDS)
        .build()
        .toString();
  }

  public String deleteAuthCookie() {
    return ResponseCookie.from(COOKIE_NAME, "")
        .secure(true)
        .httpOnly(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(0)
        .build()
        .toString();
  }
}
