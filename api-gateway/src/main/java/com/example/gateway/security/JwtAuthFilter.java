package com.example.gateway.security;

import com.example.common.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    // Do NOT try to validate JWT for register/login
    return path.equals("/api/members/register") || path.equals("/api/members/login");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String token = resolveToken(request);
    if (token != null) {
      try {
        String userId = jwtService.validateAndGetSubject(token);
        var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.setAttribute("userId", Long.valueOf(userId));
      } catch (Exception e) {
        // invalid token -> just ignore, request will be treated as anonymous
      }
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest req) {
    if (req.getCookies() != null) {
      Cookie jwtCookie = Arrays.stream(req.getCookies())
          .filter(c -> "JWT".equals(c.getName()))
          .findFirst()
          .orElse(null);
      if (jwtCookie != null && !jwtCookie.getValue().isBlank()) {
        return jwtCookie.getValue();
      }
    }
    String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}