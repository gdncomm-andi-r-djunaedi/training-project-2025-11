package com.blibli.gdn.memberService.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingFilter Unit Tests")
class LoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private LoggingFilter loggingFilter;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate trace ID when not present in header")
    void testDoFilter_GenerateTraceId() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Trace-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/members");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(200);

        // When
        loggingFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, times(1)).setHeader(eq("X-Trace-Id"), anyString());
        assertNull(MDC.get("traceId")); // Should be cleared after filter
    }

    @Test
    @DisplayName("Should use existing trace ID from header")
    void testDoFilter_UseExistingTraceId() throws ServletException, IOException {
        // Given
        String existingTraceId = "existing-trace-id-123";
        when(request.getHeader("X-Trace-Id")).thenReturn(existingTraceId);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/members");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(response.getStatus()).thenReturn(201);

        // When
        loggingFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, times(1)).setHeader("X-Trace-Id", existingTraceId);
        assertNull(MDC.get("traceId")); // Should be cleared after filter
    }

    @Test
    @DisplayName("Should handle filter chain exception")
    void testDoFilter_HandleException() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Trace-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/members");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            loggingFilter.doFilter(request, response, filterChain);
        });
        
        // MDC should still be cleared even on exception
        assertNull(MDC.get("traceId"));
    }

    @Test
    @DisplayName("Should log request and response information")
    void testDoFilter_Logging() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Trace-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/api/v1/members/123");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(response.getStatus()).thenReturn(200);

        // When
        loggingFilter.doFilter(request, response, filterChain);

        // Then
        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
        verify(request, atLeastOnce()).getRemoteAddr();
        verify(response, atLeastOnce()).getStatus();
        verify(filterChain, times(1)).doFilter(request, response);
    }
}

