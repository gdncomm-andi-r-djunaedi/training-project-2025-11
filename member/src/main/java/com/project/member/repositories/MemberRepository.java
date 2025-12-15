package com.project.member.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}