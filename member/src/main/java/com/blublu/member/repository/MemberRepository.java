package com.blublu.member.repository;

import com.blublu.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface MemberRepository extends JpaRepository<Member, Long> {
  List<Member> findByUsername(String username);
}
