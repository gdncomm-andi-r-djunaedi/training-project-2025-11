package com.gdn.training.api_gateway.security;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gdn.training.api_gateway.config.RateLimiterProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    static final String HEADER_LIMIT = "X-RateLimit-Limit";
    static final String HEADER_REMAINING = "X-RateLimit-Remaining";

    private final RateLimiterProperties properties;
    private final RateLimiterService rateLimiterService;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!properties.isEnabled()
                || properties.getRequestsPerMinute() <= 0
                || isIgnoredPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        RateLimitResult result = rateLimiterService.consume(key);
        addHeaders(response, result);

        if (!result.allowed()) {
            long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(Math.max(0, result.nanosToReset()));
            if (retryAfterSeconds > 0) {
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1, retryAfterSeconds)));
            }
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Rate limit exceeded\"}");
            response.getWriter().flush();
            log.warn("Rate limit exceeded for key={} path={}", key, request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void addHeaders(HttpServletResponse response, RateLimitResult result) {
        response.setHeader(HEADER_LIMIT, String.valueOf(rateLimiterService.getConfiguredLimit()));
        long remaining = Math.max(0, result.remainingTokens());
        response.setHeader(HEADER_REMAINING, String.valueOf(remaining));
    }

    private boolean isIgnoredPath(String path) {
        if (properties.getIgnoredPaths() == null || properties.getIgnoredPaths().isEmpty()) {
            return false;
        }
        return properties.getIgnoredPaths().stream()
                .filter(StringUtils::hasText)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String stringPrincipal && StringUtils.hasText(stringPrincipal)) {
                return stringPrincipal;
            }
        }
        return StringUtils.hasText(request.getRemoteAddr()) ? request.getRemoteAddr() : "anonymous";
    }
}

