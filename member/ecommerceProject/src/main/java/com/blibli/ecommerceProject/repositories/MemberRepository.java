package com.blibli.ecommerceProject.repositories;

import com.blibli.ecommerceProject.dto.MemberProfiledto;
import com.blibli.ecommerceProject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    Member findByEmailId(String emailId);

    @Query(value = "SELECT username AS username, email_id AS emailId, address AS address, phone_no AS phoneNo " +
                    "FROM member WHERE email_id = :memberId", nativeQuery = true)
    MemberProfiledto findDetailsByEmailId(@Param("memberId") String memberId);

}
