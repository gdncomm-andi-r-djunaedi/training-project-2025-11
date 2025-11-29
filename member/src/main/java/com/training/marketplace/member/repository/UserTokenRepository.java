package com.training.marketplace.member.repository;

import com.training.marketplace.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<MemberEntity, String> {
}
