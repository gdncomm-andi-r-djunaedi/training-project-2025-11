package com.project.member.repositories;

import com.project.member.security.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByTokenHash(String tokenHash);
}