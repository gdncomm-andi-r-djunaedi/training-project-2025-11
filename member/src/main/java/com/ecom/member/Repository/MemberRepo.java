package com.ecom.member.Repository;

import com.ecom.member.Dto.MemberDto;
import com.ecom.member.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepo extends JpaRepository<Member,Long> {

    Boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    Boolean existsByUserId(String userId);

}
