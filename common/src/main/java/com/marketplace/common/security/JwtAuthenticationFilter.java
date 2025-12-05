package com.marketplace.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.common.constants.AppConstants;
import com.marketplace.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Base JWT authentication filter for downstream services.
 * Validates JWT tokens and sets both MemberContext and Spring Security authentication.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final List<String> publicEndpoints;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String path = request.getRequestURI();

            // Allow public endpoints
            if (isPublicEndpoint(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Get token from header
            String authHeader = request.getHeader(AppConstants.JWT_HEADER);

            if (authHeader == null || !authHeader.startsWith(AppConstants.JWT_PREFIX)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring(AppConstants.JWT_PREFIX.length());

            // Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token has been invalidated");
                return;
            }

            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            // Extract member info
            UUID memberId = jwtTokenProvider.getMemberIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);

            // Set MemberContext for application use
            MemberContext context = MemberContext.builder()
                    .memberId(memberId)
                    .email(email)
                    .token(token)
                    .build();
            MemberContextHolder.setContext(context);

            // Set Spring Security authentication
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    memberId,  // principal
                    null,      // credentials (not needed after authentication)
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authenticated member: {} ({})", memberId, email);
            
            filterChain.doFilter(request, response);

        } finally {
            // Always clear the contexts after request processing
            MemberContextHolder.clearContext();
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(endpoint -> {
            if (endpoint.endsWith("/**")) {
                String prefix = endpoint.substring(0, endpoint.length() - 3);
                return path.startsWith(prefix);
            }
            return path.startsWith(endpoint);
        });
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build();
        
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
