package com.gdn.training.apigateway.infrastructure.security;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet filter that executes the AuthenticationFilterChain (Chain of
 * Responsibility)
 * for every request. If chain returns false, the filter stops processing.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiGatewayFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwt;
    private final TokenBlacklist blacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);

            if (!blacklist.isBlacklisted(token)) {
                String memberId = jwt.validate(token);
                request.setAttribute("memberId", memberId);
            }
        }

        // continue processing
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/");
    }

}
