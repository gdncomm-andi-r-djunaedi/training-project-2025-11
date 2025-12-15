package com.training.marketplace.member.repository;

import com.training.marketplace.member.entity.MemberEntity;

import java.util.Optional;

public interface MemberCustomRepository {
    Optional<MemberEntity> findUserByUsername(String username);
}
