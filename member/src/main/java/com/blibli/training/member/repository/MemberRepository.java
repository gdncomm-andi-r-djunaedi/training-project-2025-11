package com.blibli.training.member.repository;

import com.blibli.training.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
