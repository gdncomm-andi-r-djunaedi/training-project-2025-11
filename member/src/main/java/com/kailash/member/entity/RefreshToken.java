package com.kailash.member.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_tokens_jti", columnList = "jti"),
                @Index(name = "idx_refresh_tokens_member_id", columnList = "member_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    /**
     * The public id (jti) sent to client and stored in DB for lookup.
     * Keep unique.
     */
    @Column(nullable = false, unique = true)
    private String jti;

    /**
     * Owner member id (foreign key to members.id).
     * Use columnDefinition uuid for Postgres.
     */
    @Column(name = "member_id", columnDefinition = "uuid", nullable = false)
    private UUID memberId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (issuedAt == null) issuedAt = Instant.now();
    }
}
