package com.gdn.training.member.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.training.member.infrastructure.entity.MemberEntity;

public interface SpringDataJpaMemberRepository extends JpaRepository<MemberEntity, UUID> {
    Optional<MemberEntity> findByEmail(String email);

}
