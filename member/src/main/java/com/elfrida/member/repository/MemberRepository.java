package com.elfrida.member.repository;

import com.elfrida.member.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String > {
    Optional<Member> findByEmail(String email);
}
