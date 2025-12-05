package com.project.cart.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Gateway User Context Filter
 * Extracts user information from headers set by the API Gateway
 * (Gateway already validated the JWT, so we just trust the headers)
 */
@Slf4j
@Component
public class GatewayUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extract user info from headers set by Gateway
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");

        log.debug("GatewayUserContextFilter - Received headers: X-User-Id={}, X-Username={}", userId, username);

        // If headers are present, set authentication context
        if (username != null && !username.isEmpty()) {
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("") // No password needed, Gateway already authenticated
                    .authorities(new ArrayList<>())
                    .build();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("Set authentication context for user: {} (userId: {})", username, userId);
        } else {
            log.warn("No X-Username header found - authentication not set. Path: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
