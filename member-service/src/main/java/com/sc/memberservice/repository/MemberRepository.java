package com.sc.memberservice.repository;

import com.sc.memberservice.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Member findByUsername(String username);

    Optional<Member> findByEmail(String email);

}
