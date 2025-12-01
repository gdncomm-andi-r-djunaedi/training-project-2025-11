package com.example.gateway.security;

import com.example.common.security.JwtService;
import com.example.gateway.properties.GatewaySecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final GatewaySecurityProperties securityProps;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public JwtAuthFilter(JwtService jwtService, GatewaySecurityProperties securityProps) {
    this.jwtService = jwtService;
    this.securityProps = securityProps;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();

    if (securityProps.getPublicPaths() == null) {
      return false;
    }

    // If path matches any configured public path, we don't need to run JWT filter
    return securityProps.getPublicPaths().stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
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
        // invalid token -> ignore, let security rules handle it
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