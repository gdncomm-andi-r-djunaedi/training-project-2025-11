package com.example.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    @Value("${jwt.secret}")
    private String secret;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/member/register",
            "/api/member/login"
    );

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("JWT filter invoked for path: {}", exchange.getRequest().getPath());

            var request = exchange.getRequest();
            String path = request.getPath().toString();

            if (PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)) {
                log.info("Skipping JWT filter for public path: {}", path);
                return chain.filter(exchange);
            }

            String token = null;

            if (request.getCookies().containsKey("Authorization")) {
                var cookie = request.getCookies().getFirst("Authorization");
                if (cookie != null) {
                    token = cookie.getValue();
                    log.debug("Token found in cookies");
                }
            } else if (request.getHeaders().containsKey("Authorization")) {
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    log.debug("Token found in Authorization header");
                }
            }

            if (token == null) {
                log.warn("No JWT token found in request for path: {}", exchange.getRequest().getPath());
                return unauthorized(exchange);
            }

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                log.info("JWT validated successfully for user: {}", claims.get("email", String.class));

                var mutatedRequest = request.mutate()
                        .header("X-User-Name", claims.get("name", String.class))
                        .header("X-User-Id", claims.get("id", String.class))
                        .header("X-User-Email", claims.get("email", String.class))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (io.jsonwebtoken.security.SignatureException e) {
                log.error("JWT signature validation failed for path: {}, error: {}",
                        exchange.getRequest().getPath(), e.getMessage());
                return unauthorized(exchange);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.error("JWT token expired for path: {}", exchange.getRequest().getPath());
                return unauthorized(exchange);
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                log.error("JWT token malformed for path: {}, error: {}",
                        exchange.getRequest().getPath(), e.getMessage());
                return unauthorized(exchange);
            } catch (Exception e) {
                log.error("JWT validation failed for path: {}, error: {}, class: {}",
                        exchange.getRequest().getPath(), e.getMessage(), e.getClass().getName());
                e.printStackTrace();
                return unauthorized(exchange);
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Put configuration properties here if needed
    }
}
