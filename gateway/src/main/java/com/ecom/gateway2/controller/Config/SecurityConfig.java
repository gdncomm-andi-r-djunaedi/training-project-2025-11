package com.ecom.gateway2.controller.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class SecurityConfig {

    @Bean
    public SecretKey secretKey() {
        String secret = "MySuperSecretKeyForJWTTokenGenerationMustBeAtLeast32BytesLong";
        return new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    }
}


