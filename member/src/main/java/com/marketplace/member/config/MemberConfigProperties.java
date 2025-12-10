package com.marketplace.member.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Member Service
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "member")
public class MemberConfigProperties {

    private Security security = new Security();
    private Registration registration = new Registration();

    @Data
    public static class Security {
        private int passwordMinLength = 8;
        private int maxLoginAttempts = 5;
        private int lockoutDurationMinutes = 30;
    }

    @Data
    public static class Registration {
        private boolean emailVerificationRequired = false;
        private int usernameMinLength = 3;
        private int usernameMaxLength = 50;
    }
}
