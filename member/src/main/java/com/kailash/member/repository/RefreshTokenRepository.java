package com.kailash.member.repository;

import com.kailash.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByJti(String jti);
    List<RefreshToken> findByMemberId(UUID memberId);
    void deleteByMemberId(UUID memberId);
}
