package com.gdn.training.apigateway.infrastructure.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {
    private final JwtProviderFactory jwtProviderFactory;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public JwtAuthenticationFilter(JwtProviderFactory jwtProviderFactory,
            TokenBlacklistRepository tokenBlacklistRepository) {
        this.jwtProviderFactory = jwtProviderFactory;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    public boolean handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return true; // allow public access
        }

        JwtProvider provider = jwtProviderFactory.defaultProvider();
        if (provider == null || !provider.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        }

        Optional<Map<String, Object>> claims = provider.parseClaims(token);
        if (claims.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid claims");
            return false;
        }

        Map<String, Object> claim = claims.get();
        Object jti = claim.get("jti");

        // Fixed logic: check if jti exists AND if it's blacklisted
        if (jti != null && tokenBlacklistRepository.isBlacklisted((String) jti)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revoked");
            return false;
        }

        request.setAttribute("auth.claims", claim);
        return true;
    }

    private String resolveToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("JWT".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }

        return null;
    }
}