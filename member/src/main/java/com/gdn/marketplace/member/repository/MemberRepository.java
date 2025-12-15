package com.gdn.marketplace.member.repository;

import com.gdn.marketplace.member.entity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByUsername(String username);
}
