package com.yuna.member.helper;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public class HeaderHelper {
  public static Optional<String> getUserName(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader("username"));
  }
}
