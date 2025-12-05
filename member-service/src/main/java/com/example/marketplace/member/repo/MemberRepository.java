package com.example.marketplace.member.repo;

import com.example.marketplace.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
