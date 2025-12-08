package com.marketplace.gateway.filter;

import com.marketplace.gateway.config.JwtProperties;
import com.marketplace.gateway.exception.JwtValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtProperties jwtProperties;

    public JwtAuthenticationFilter(JwtProperties jwtProperties) {
        super(Config.class);
        this.jwtProperties = jwtProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            try {
                String token = extractToken(request);
                if (!StringUtils.hasText(token)) {
                    return handleUnauthorized(exchange, "Missing authentication token");
                }

                Claims claims = validateAndParseToken(token);
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Email", claims.get("email", String.class))
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (JwtValidationException e) {
                return handleUnauthorized(exchange, e.getMessage());
            } catch (Exception e) {
                log.error("Error processing JWT token", e);
                return handleUnauthorized(exchange, "Invalid authentication token");
            }
        };
    }

    private String extractToken(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst(jwtProperties.getCookieName());
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            return cookie.getValue();
        }

        List<String> authHeaders = request.getHeaders().get(jwtProperties.getHeaderName());
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith(jwtProperties.getHeaderPrefix())) {
                return authHeader.substring(jwtProperties.getHeaderPrefix().length()).trim();
            }
            return authHeader.trim();
        }
        return null;
    }

    private Claims validateAndParseToken(String token) {
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)
            );

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration() != null && claims.getExpiration().before(new java.util.Date())) {
                throw new JwtValidationException("Token has expired");
            }

            return claims;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new JwtValidationException("Token has expired");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new JwtValidationException("Invalid token signature");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new JwtValidationException("Malformed token");
        } catch (Exception e) {
            throw new JwtValidationException("Token validation failed: " + e.getMessage());
        }
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
    }
}

