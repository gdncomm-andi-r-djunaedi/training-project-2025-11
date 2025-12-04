package com.blibi.blibligatway.filter;

import com.blibi.blibligatway.security.JwtUtil;
import com.blibi.blibligatway.security.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
public class LogoutGatewayFilterFactory extends AbstractGatewayFilterFactory<LogoutGatewayFilterFactory.Config> {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public LogoutGatewayFilterFactory(ObjectMapper objectMapper, JwtUtil jwtUtil, 
                                      TokenBlacklistService tokenBlacklistService) {
        super(Config.class);
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("Logout handler - processing logout request");
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

//            extract token
            String token = extractJwtFromRequest(request);

            // check acces token and black list it
            if (StringUtils.hasText(token)) {
                try {
                    if (jwtUtil.validateToken(token)) {
                        Claims claims = jwtUtil.getAllClaimsFromToken(token);
                        long expirationTime = claims.getExpiration().getTime();
                        tokenBlacklistService.blacklistToken(token, expirationTime);
                        log.info("Token blacklisted for user: {}", claims.getSubject());
                    }
                } catch (Exception e) {
                    log.warn("Could not blacklist token: {}", e.getMessage());
                }
            }

            // check refresh token and black list it
            HttpCookie refreshCookie = request.getCookies().getFirst("refreshToken");
            if (refreshCookie != null && StringUtils.hasText(refreshCookie.getValue())) {
                String refreshToken = refreshCookie.getValue();
                try {
                    if (jwtUtil.validateToken(refreshToken) && jwtUtil.isRefreshToken(refreshToken)) {
                        Claims claims = jwtUtil.getAllClaimsFromToken(refreshToken);
                        long expirationTime = claims.getExpiration().getTime();
                        tokenBlacklistService.blacklistToken(refreshToken, expirationTime);
                        log.info("Refresh token blacklisted for user: {}", claims.getSubject());
                    }
                } catch (Exception e) {
                    log.warn("Could not blacklist refresh token: {}", e.getMessage());
                }
            }

            // Clear JWT cookie by setting it to expire immediately
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                    .maxAge(Duration.ofSeconds(0))
                    .httpOnly(true)
                    .secure(false) // Set to true in production with HTTPS
                    .path("/")
                    .sameSite("Strict")
                    .build();

            // Clear refresh token cookie
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(Duration.ofSeconds(0))
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Strict")
                    .build();

            response.addCookie(jwtCookie);
            response.addCookie(refreshTokenCookie);
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            // build success response
            try {
                ObjectNode jsonResponse = objectMapper.createObjectNode();
                jsonResponse.put("success", true);
                jsonResponse.put("message", "Logout successful");

                String responseBody = objectMapper.writeValueAsString(jsonResponse);
                DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
                
                return response.writeWith(Mono.just(buffer));
            } catch (Exception e) {
                log.error("Error creating logout response: {}", e.getMessage(), e);
                String errorResponse = "{\"success\":true,\"message\":\"Logout successful\"}";
                DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
                return response.writeWith(Mono.just(buffer));
            }
        };
    }

    private String extractJwtFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        HttpCookie jwtCookie = request.getCookies().getFirst("jwt");
        if (jwtCookie != null && StringUtils.hasText(jwtCookie.getValue())) {
            return jwtCookie.getValue();
        }

        return null;
    }
}
