package com.gdn.member.repository;

import com.gdn.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, String> {
    boolean existsByEmail(String email);
    Member findByEmail(String email);
    @Query(value = "SELECT m.password from member m where m.email=:email",nativeQuery = true)
    String findPasswordByEmail(String email);

}
