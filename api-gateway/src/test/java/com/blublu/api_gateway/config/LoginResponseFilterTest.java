package com.blublu.api_gateway.config;

import com.blublu.api_gateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class LoginResponseFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    private LoginResponseFilter loginResponseFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginResponseFilter = new LoginResponseFilter(jwtUtil);
    }

    @Test
    void testDoFilterInternal_LoginRequest_AddsToken() throws ServletException, IOException {
        // Setup
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.generateToken(anyString())).thenReturn("mocked-jwt-token");

        FilterChain filterChain = (req, res) -> {
            res.setContentType("application/json");
            PrintWriter writer = res.getWriter();
            writer.write("{\"username\": \"testuser\"}");
            writer.flush();
        };

        // Execute
        loginResponseFilter.doFilter(request, response, filterChain);

        // Verify
        String content = response.getContentAsString();
        assertTrue(content.contains("mocked-jwt-token"), "Response should contain the generated token");
        assertTrue(content.contains("testuser"), "Response should still contain the username");
    }

    @Test
    void testDoFilterInternal_NotLoginRequest_NoToken() throws ServletException, IOException {
        // Setup
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/other");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            res.setContentType("application/json");
            PrintWriter writer = res.getWriter();
            writer.write("{\"username\": \"testuser\"}");
            writer.flush();
        };

        // Execute
        loginResponseFilter.doFilter(request, response, filterChain);

        // Verify
        String content = response.getContentAsString();
        assertTrue(!content.contains("mocked-jwt-token"), "Response should NOT contain the generated token");
    }
}
