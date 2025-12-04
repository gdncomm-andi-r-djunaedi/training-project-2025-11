package com.microservice.api_gateway.config;

import com.microservice.api_gateway.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.startsWith("/api/member/")) {
            System.out.println("JwtFilter - Skipping filter for member endpoint: " + path);
            return true;
        }

        if (path.startsWith("/api/products/") || path.startsWith("/api/products")) {
            System.out.println("JwtFilter - Skipping filter for product endpoint: " + path);
            return true;
        }

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/api-docs")) {
            System.out.println("JwtFilter - Skipping filter for documentation endpoint: " + path);
            return true;
        }

        if (path.startsWith("/api/test/")) {
            System.out.println("JwtFilter - Skipping filter for test endpoint: " + path);
            return true;
        }

        if (path.startsWith("/api/cart/")) {
            System.out.println("JwtFilter - Processing JWT for cart endpoint: " + path);
            return false;
        }

        System.out.println("JwtFilter - Processing JWT for endpoint: " + path);
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String requestPath = request.getRequestURI();

        System.out.println("=== JwtFilter START ===");
        System.out.println("JwtFilter - Path: " + requestPath + ", AuthHeader: " + (authHeader != null ? "Present" : "Missing"));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("JwtFilter - Token extracted, length: " + token.length());

            try {
                Long userId = jwtService.extractUserId(token);
                System.out.println("JwtFilter - Extracted userId: " + userId);

                if (userId == null) {
                    System.err.println("JwtFilter - Token does not contain userId");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token does not contain userId. Please login again.\"}");
                    return;
                }

                boolean expired = jwtService.isTokenExpired(token);
                System.out.println("JwtFilter - Token expired: " + expired);

                if (expired) {
                    System.err.println("JwtFilter - Token has expired");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token has expired. Please login again.\"}");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                String.valueOf(userId),
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("USER"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("JwtFilter - Authentication set for userId: " + userId);

                org.springframework.security.core.Authentication verify =
                        SecurityContextHolder.getContext().getAuthentication();
                System.out.println("JwtFilter - Verification - Authentication: " + (verify != null ? verify.getPrincipal() : "NULL"));
                System.out.println("JwtFilter - Verification - IsAuthenticated: " + (verify != null && verify.isAuthenticated()));

            } catch (Exception e) {
                System.err.println("JWT Filter Error: " + e.getMessage());
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid or corrupted token. Please login again.\", \"details\": \"" + e.getMessage() + "\"}");
                return;
            }
        } else {
            System.err.println("JwtFilter - No Authorization header found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication required. Please provide a valid JWT token.\"}");
            return;
        }

        System.out.println("JwtFilter - Continuing filter chain...");
        filterChain.doFilter(request, response);
        System.out.println("JwtFilter - Filter chain completed");
        System.out.println("=== JwtFilter END ===");
    }
}