package com.gdn.training.member.repository;

import com.gdn.training.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    java.util.Optional<Member> findByUsername(String username);


}
