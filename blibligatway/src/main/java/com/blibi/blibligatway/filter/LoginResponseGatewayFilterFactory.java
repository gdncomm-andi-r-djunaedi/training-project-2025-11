package com.blibi.blibligatway.filter;

import com.blibi.blibligatway.dto.LoginResponse;
import com.blibi.blibligatway.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class LoginResponseGatewayFilterFactory extends AbstractGatewayFilterFactory<LoginResponseGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    @Value("${jwt.cookie.secure:false}")
    private boolean cookieSecure;

    public LoginResponseGatewayFilterFactory(JwtUtil jwtUtil, ObjectMapper objectMapper,
                                             ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.modifyResponseBodyFilter = modifyResponseBodyFilter;
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        log.info("LoginResponse filter factory - creating filter");
        
        ModifyResponseBodyGatewayFilterFactory.Config modifyConfig = new ModifyResponseBodyGatewayFilterFactory.Config();
        modifyConfig.setInClass(String.class);
        modifyConfig.setOutClass(String.class);
        modifyConfig.setNewContentType(MediaType.APPLICATION_JSON_VALUE);
        modifyConfig.setRewriteFunction(String.class, String.class, (exchange, originalBody) -> {
            log.info("LoginResponse filter - Rewrite function called with body: {}", originalBody);
            
            try {
                JsonNode jsonNode = objectMapper.readTree(originalBody);
                
                if (jsonNode.has("success") && jsonNode.get("success").asBoolean() 
                    && jsonNode.has("data") && jsonNode.get("data").isObject()) {
                    
                    JsonNode dataNode = jsonNode.get("data");
                    String userId = dataNode.has("id") ? dataNode.get("id").asText() : null;
                    String email = dataNode.has("email") ? dataNode.get("email").asText() : null;
                    
                    List<String> roles = new ArrayList<>();
                    if (dataNode.has("roles") && dataNode.get("roles").isArray()) {
                        dataNode.get("roles").forEach(role -> roles.add(role.asText()));
                    }
//               set customer role if no role found
                    if (roles.isEmpty()) {
                        roles.add("CUSTOMER");
                    }
                    
                    if (userId != null && email != null) {
                        String token = jwtUtil.generateToken(userId, email, roles);
                        String refreshToken = jwtUtil.generateRefreshToken(userId);
                        
//                    set jwt cookie
                        long cookieMaxAge = expiration / 1000; // Convert to seconds
                        ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                                .maxAge(Duration.ofSeconds(cookieMaxAge))
                                .httpOnly(true)
                                .secure(cookieSecure) // Set to true in production with HTTPS
                                .path("/")
                                .sameSite("Strict")
                                .build();
                        
//            set refresh token cookie
                        long refreshCookieMaxAge = jwtUtil.getRefreshExpiration() / 1000;
                        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                                .maxAge(Duration.ofSeconds(refreshCookieMaxAge))
                                .httpOnly(true)
                                .secure(cookieSecure)
                                .path("/")
                                .sameSite("Strict")
                                .build();
                        
                        exchange.getResponse().addCookie(jwtCookie);
                        exchange.getResponse().addCookie(refreshCookie);
                        
                        // Build login response with token, userid, role
                        // full member respons in data field
                        LoginResponse loginResponse = LoginResponse.builder()
                                .success(true)
                                .message("Login successful")
                                .token(token)
                                .userId(userId)
                                .email(email)
                                .roles(roles)
                                .data(dataNode) // Full member data
                                .build();
                        
                        String transformedBody = objectMapper.writeValueAsString(loginResponse);
                        log.info("LoginResponse filter - Transformed response and set cookies");
                        
                        return Mono.just(transformedBody);
                    }
                }
                
                return Mono.just(originalBody);
            } catch (Exception e) {
                log.error("LoginResponse filter error: {}", e.getMessage(), e);
                return Mono.just(originalBody);
            }
        });
        
        return modifyResponseBodyFilter.apply(modifyConfig);
    }
}
