package com.gdn.training.member.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.gdn.training.member.domain.model.Member;

public interface MemberRepository {
    Member save(Member member);

    Optional<Member> findById(UUID id);

    Optional<Member> findByEmail(String email);

    long count();

}
