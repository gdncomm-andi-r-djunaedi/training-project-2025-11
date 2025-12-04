package com.microservice.api_gateway.filter;

import com.microservice.api_gateway.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@Order(2)
public class CartUserIdGatewayFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Only process cart endpoints - skip all others
        boolean shouldSkip = !path.startsWith("/api/cart");
        
        if (shouldSkip) {
            System.out.println("CartUserIdGatewayFilter - shouldNotFilter: SKIPPING for path: " + path);
        } else {
            System.out.println("CartUserIdGatewayFilter - shouldNotFilter: PROCESSING for path: " + path);
        }
        
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        System.out.println("=== CartUserIdGatewayFilter START ===");
        System.out.println("CartUserIdGatewayFilter - doFilterInternal called for path: " + requestPath);
        System.out.println("CartUserIdGatewayFilter - Request method: " + request.getMethod());

        // This should always be a cart endpoint due to shouldNotFilter
        if (requestPath != null && requestPath.startsWith("/api/cart")) {
            System.out.println("CartUserIdGatewayFilter - Cart endpoint detected: " + requestPath);

            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

            System.out.println("CartUserIdGatewayFilter - Authentication: " + (auth != null ? auth.getPrincipal() : "NULL"));

            if (auth == null) {
                System.err.println("CartUserIdGatewayFilter - ERROR: No authentication found!");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required. Please provide a valid JWT token.");
                return;
            }

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("CartUserIdGatewayFilter - ERROR: No Authorization header");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authorization header is missing. Please provide a valid JWT token.");
                return;
            }

            String token = authHeader.substring(7);

            try {
                if (jwtService.isTokenExpired(token)) {
                    System.err.println("CartUserIdGatewayFilter - ERROR: Token expired");
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Token has expired. Please login again.");
                    return;
                }

                Long userId = jwtService.extractUserId(token);
                System.out.println("CartUserIdGatewayFilter - Extracted userId: " + userId);

                if (userId == null) {
                    System.err.println("CartUserIdGatewayFilter - ERROR: userId is null");
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Token does not contain userId. Please login again.");
                    return;
                }

                HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                    private final Map<String, String> customHeaders = new HashMap<>();
                    private final String userIdValue = String.valueOf(userId);

                    {
                        customHeaders.put("X-User-Id", userIdValue);
                        System.out.println("CartUserIdGatewayFilter - Added X-User-Id header: " + userIdValue);
                    }

                    @Override
                    public String getHeader(String name) {
                        String lowerName = name.toLowerCase();
                        if ("x-user-id".equals(lowerName)) {
                            System.out.println("CartUserIdGatewayFilter - getHeader('" + name + "') called, returning: " + userIdValue);
                            return userIdValue;
                        }
                        String value = super.getHeader(name);
                        System.out.println("CartUserIdGatewayFilter - getHeader('" + name + "') = " + value);
                        return value;
                    }

                    @Override
                    public Enumeration<String> getHeaderNames() {
                        List<String> names = Collections.list(super.getHeaderNames());
                        if (!names.contains("X-User-Id") && !names.contains("x-user-id")) {
                            names.add("X-User-Id");
                        }
                        System.out.println("CartUserIdGatewayFilter - getHeaderNames() called, total headers: " + names.size());
                        return Collections.enumeration(names);
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        String lowerName = name.toLowerCase();
                        if ("x-user-id".equals(lowerName)) {
                            System.out.println("CartUserIdGatewayFilter - getHeaders('" + name + "') called, returning: " + userIdValue);
                            return Collections.enumeration(Collections.singletonList(userIdValue));
                        }
                        return super.getHeaders(name);
                    }

                    @Override
                    public long getDateHeader(String name) {
                        if ("x-user-id".equals(name.toLowerCase())) {
                            return -1;
                        }
                        return super.getDateHeader(name);
                    }

                    @Override
                    public int getIntHeader(String name) {
                        if ("x-user-id".equals(name.toLowerCase())) {
                            try {
                                return Integer.parseInt(userIdValue);
                            } catch (NumberFormatException e) {
                                return -1;
                            }
                        }
                        return super.getIntHeader(name);
                    }
                };

                System.out.println("CartUserIdGatewayFilter - Continuing filter chain with wrapped request...");
                filterChain.doFilter(wrappedRequest, response);

                System.out.println("CartUserIdGatewayFilter - Filter chain completed");
                System.out.println("=== CartUserIdGatewayFilter END ===");

            } catch (Exception e) {
                System.err.println("CartUserIdGatewayFilter - EXCEPTION: " + e.getMessage());
                e.printStackTrace();
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid or corrupted token. Please login again.");
                return;
            }
        } else {
            System.out.println("CartUserIdGatewayFilter - Non-cart endpoint, passing through");
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Helper method to send error responses with consistent format
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage)
            throws IOException {
        System.err.println("CartUserIdGatewayFilter - Sending error: " + statusCode + " - " + errorMessage);
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"error\": \"%s\", \"status\": %d, \"timestamp\": \"%s\"}",
                errorMessage,
                statusCode,
                java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}