package com.zasura.member.repository;

import com.zasura.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
  boolean existsByEmail(String email);

  boolean existsByPhoneNumber(String phoneNumber);

  Member findByEmail(String email);
}
