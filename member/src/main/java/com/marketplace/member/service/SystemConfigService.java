package com.marketplace.member.service;

import com.marketplace.member.entity.SystemConfig;
import com.marketplace.member.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing system configurations stored in database.
 * Sensitive configs like RSA keys are stored here for better security.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    /**
     * Get config value by key with caching.
     * Cache is used to avoid frequent DB calls for keys.
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    @Transactional(readOnly = true)
    public Optional<String> getConfigValue(String configKey) {
        log.debug("Loading config from database: {}", configKey);
        return systemConfigRepository.findByConfigKeyAndIsActiveTrue(configKey)
                .map(SystemConfig::getConfigValue);
    }

    /**
     * Get JWT private key from database.
     */
    public Optional<String> getJwtPrivateKey() {
        return getConfigValue(SystemConfig.JWT_PRIVATE_KEY);
    }

    /**
     * Get JWT public key from database.
     */
    public Optional<String> getJwtPublicKey() {
        return getConfigValue(SystemConfig.JWT_PUBLIC_KEY);
    }

    /**
     * Save or update a config value.
     */
    @Transactional
    public void saveConfig(String configKey, String configValue, String description) {
        SystemConfig config = systemConfigRepository.findByConfigKeyAndIsActiveTrue(configKey)
                .orElseGet(() -> SystemConfig.builder()
                        .configKey(configKey)
                        .isActive(true)
                        .build());
        
        config.setConfigValue(configValue);
        config.setDescription(description);
        systemConfigRepository.save(config);
        
        log.info("System config saved: {}", configKey);
    }

    /**
     * Check if a config exists.
     */
    @Transactional(readOnly = true)
    public boolean configExists(String configKey) {
        return systemConfigRepository.existsByConfigKey(configKey);
    }
}

