package com.marketplace.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing sensitive system configurations like RSA keys.
 * Access to this table should be restricted at database level.
 * 
 * PostgreSQL access restriction:
 * - Create a dedicated role for key access
 * - REVOKE ALL ON system_configs FROM PUBLIC;
 * - GRANT SELECT ON system_configs TO member_service_app;
 */
@Entity
@Table(name = "system_configs", indexes = {
        @Index(name = "idx_system_config_key", columnList = "config_key", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_encrypted", nullable = false)
    @Builder.Default
    private Boolean isEncrypted = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Config key constants
    public static final String JWT_PRIVATE_KEY = "JWT_PRIVATE_KEY";
    public static final String JWT_PUBLIC_KEY = "JWT_PUBLIC_KEY";
}

