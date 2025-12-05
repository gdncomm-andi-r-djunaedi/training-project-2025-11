package com.wijaya.commerce.member.modelDb;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.wijaya.commerce.member.constant.CollectionMember;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = CollectionMember.COLLECTION_SESSION_MANAGER)
public class SessionManagerModelDb {

    @Id
    private String id;
    private String memberId;
    private String accessToken;
    private String refreshToken;
    private String status; // active, expired, revoked
    private LocalDateTime accessTokenExpiresAt;
    private LocalDateTime refreshTokenExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}
