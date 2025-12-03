package com.gdn.training.member.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Member {

    private final UUID id;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final String phoneNumber;
    private final String avatarUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Member(UUID id, String fullName, String email, String passwordHash, String phoneNumber, String avatarUrl,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
