package com.gdn.training.api_gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.core.io.ClassPathResource;
import java.security.interfaces.RSAPublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // PUBLIC ENDPOINTS - No JWT required
                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/api/v1/member/auth/**", // register/login
                                "/api/v1/products/**" // list products
                        ).permitAll()

                        // Everything else needs JWT
                        .anyExchange().authenticated())

                // JWT Validation - only for authenticated routes
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder())))

                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        try {
            String key = new String(new ClassPathResource("jwt/public.pem")
                    .getInputStream().readAllBytes());

            key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(decoded));

            return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA public key for JWT", e);
        }
    }
}
