package com.blublu.api_gateway.config;

import com.blublu.api_gateway.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Map;

public class LoginResponseFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginResponseFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if it's a login request (adjust path as needed)
        if (request.getRequestURI().contains("/login") && request.getMethod().equalsIgnoreCase("POST")) {

            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

            filterChain.doFilter(request, responseWrapper);

            // Only process if status is OK (200)
            if (responseWrapper.getStatus() == HttpServletResponse.SC_OK) {
                byte[] responseBody = responseWrapper.getContentAsByteArray();
                if (responseBody.length > 0) {
                    try {
                        Map<String, Object> body = objectMapper.readValue(responseBody, Map.class);

                        if (body.containsKey("username")) {
                            String username = (String) body.get("username");
                            String token = jwtUtil.generateToken(username);

                            // Add token to response body
                            body.put("token", token);

                            // Write modified body back to response
                            byte[] newBody = objectMapper.writeValueAsBytes(body);
                            response.setContentLength(newBody.length);
                            response.getOutputStream().write(newBody);
                        } else {
                            responseWrapper.copyBodyToResponse();
                        }
                    } catch (Exception e) {
                        // In case of parsing error, just copy original body
                        responseWrapper.copyBodyToResponse();
                    }
                } else {
                    responseWrapper.copyBodyToResponse();
                }
            } else {
                responseWrapper.copyBodyToResponse();
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
