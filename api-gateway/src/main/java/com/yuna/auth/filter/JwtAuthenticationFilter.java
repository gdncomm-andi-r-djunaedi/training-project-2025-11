package com.yuna.auth.filter;

import com.yuna.auth.model.User;
import com.yuna.auth.service.impl.JwtService;
import com.yuna.auth.service.impl.TokenServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final HandlerExceptionResolver handlerExceptionResolver;
  private final JwtService jwtService;
  private final TokenServiceImpl tokenServiceImpl;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final Optional<Cookie> authCookie = Optional.ofNullable(request.getCookies()).flatMap(cookies -> Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("token")).findFirst());

    if ((authHeader == null || !authHeader.startsWith("Bearer ")) && authCookie.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String jwt;
      if (authCookie.isPresent()) {
        jwt = authCookie.get().getValue();
      } else {
        jwt = authHeader.substring(7);
      }
      final String username = jwtService.extractUsername(jwt);

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (username != null && authentication == null) {
        if (jwtService.isTokenValid(jwt) && tokenServiceImpl.isTokenValid(jwt)) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              User.builder().username(username).build(),
              null,
              null
          );

          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

      filterChain.doFilter(request, response);
    } catch (Exception exception) {
      handlerExceptionResolver.resolveException(request, response, null, exception);
    }
  }
}
