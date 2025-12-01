package com.example.gateway.security;

import com.example.gateway.properties.GatewayRateLimitProperties;
import com.example.gateway.properties.RateLimitRuleProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

  private final GatewayRateLimitProperties rateLimitProps;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  /**
   * Key: ruleKey (pathPattern) + ":" + clientKey
   * Value: window info (startEpochSec, count)
   */
  private final Map<String, Window> counters = new ConcurrentHashMap<>();

  public RateLimitFilter(GatewayRateLimitProperties rateLimitProps) {
    this.rateLimitProps = rateLimitProps;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    if (rateLimitProps.getRules() == null || rateLimitProps.getRules().isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getServletPath();
    String clientIp = extractClientIp(request);

    for (RateLimitRuleProperties rule : rateLimitProps.getRules()) {
      if (pathMatcher.match(rule.getPathPattern(), path)) {
        String key = rule.getPathPattern() + ":" + clientIp;
        long nowSec = Instant.now().getEpochSecond();
        long windowSize = rule.getWindowSeconds();
        long limit = rule.getRequests();

        Window window = counters.compute(key, (k, existing) -> {
          if (existing == null) {
            return new Window(nowSec, 1);
          }
          if (nowSec - existing.startSec >= windowSize) {
            // new window
            return new Window(nowSec, 1);
          }
          // same window
          if (existing.count >= limit) {
            return existing; // keep as is, we'll reject
          }
          return new Window(existing.startSec, existing.count + 1);
        });

        if (window.count > limit || (window.count == limit && nowSec - window.startSec < windowSize)) {
          response.setStatus(429);
          response.setContentType("application/json");
          response.getWriter().write("""
              {"error":"too_many_requests","message":"Rate limit exceeded"}
              """);
          return;
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private record Window(long startSec, long count) {}
}
