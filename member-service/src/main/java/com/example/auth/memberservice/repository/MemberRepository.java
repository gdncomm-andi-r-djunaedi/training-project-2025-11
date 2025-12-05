package com.example.auth.memberservice.repository;

import com.example.auth.memberservice.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {

  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);
}