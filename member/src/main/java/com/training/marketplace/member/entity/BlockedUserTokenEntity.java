package com.training.marketplace.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blocked_user_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockedUserTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "auth_token", nullable = false)
    private String authToken;
}
