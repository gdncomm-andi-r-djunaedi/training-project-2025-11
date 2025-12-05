package com.marketplace.member.repository;

import com.marketplace.member.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    Optional<SystemConfig> findByConfigKeyAndIsActiveTrue(String configKey);
    
    boolean existsByConfigKey(String configKey);
}

