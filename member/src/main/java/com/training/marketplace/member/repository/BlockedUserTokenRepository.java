package com.training.marketplace.member.repository;

import com.training.marketplace.member.entity.BlockedUserTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedUserTokenRepository extends JpaRepository<BlockedUserTokenEntity, String> {
}
