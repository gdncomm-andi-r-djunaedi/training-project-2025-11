package com.training.marketplace.member.repository;

import com.training.marketplace.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, String>, MemberCustomRepository {
}
