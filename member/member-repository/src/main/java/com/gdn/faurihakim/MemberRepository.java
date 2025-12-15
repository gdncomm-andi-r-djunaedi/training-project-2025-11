package com.gdn.faurihakim;

import com.gdn.faurihakim.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByEmailOrPhoneNumber(String email, String phoneNumber);

    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findByEmail(String email);
}