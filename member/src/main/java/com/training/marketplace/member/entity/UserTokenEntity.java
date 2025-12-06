package com.training.marketplace.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenEntity {
    @Id
    @Column(name = "id", nullable = false)
    private String memberId;

    @Id
    @Column(name = "auth_token", nullable = false)
    private String authToken;
}
