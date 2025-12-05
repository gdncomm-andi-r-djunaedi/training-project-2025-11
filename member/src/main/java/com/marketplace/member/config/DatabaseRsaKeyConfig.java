package com.marketplace.member.config;

import com.marketplace.common.security.RsaKeyProperties;
import com.marketplace.member.entity.SystemConfig;
import com.marketplace.member.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration to load RSA keys from database instead of application.yml.
 * This provides better security by keeping private keys out of config files.
 * 
 * The private key is loaded from database (system_configs table).
 * The public key can still be loaded from config for faster access.
 */
@Slf4j
@Configuration
public class DatabaseRsaKeyConfig {

    @Value("${jwt.rsa.public-key}")
    private String publicKeyFromConfig;

    /**
     * Creates RsaKeyProperties bean that loads private key from database.
     * This bean is marked as @Primary to override the default from common module.
     */
    @Bean
    @Primary
    public RsaKeyProperties rsaKeyProperties(SystemConfigRepository systemConfigRepository) {
        log.info("Loading RSA keys - public from config, private from database");

        // Get private key from database
        String privateKey = systemConfigRepository
                .findByConfigKeyAndIsActiveTrue(SystemConfig.JWT_PRIVATE_KEY)
                .map(SystemConfig::getConfigValue)
                .orElseGet(() -> {
                    log.warn("Private key not found in database, keys will be seeded on first run");
                    return "";
                });

        return new RsaKeyProperties(publicKeyFromConfig, privateKey);
    }
}

