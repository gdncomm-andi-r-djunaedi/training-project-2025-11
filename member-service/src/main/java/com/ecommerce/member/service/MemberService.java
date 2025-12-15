package com.ecommerce.member.service;

import com.ecommerce.member.entity.Member;
import com.ecommerce.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Autowired
    MemberRepository memberRepository;

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
    }

    public Member updateMember(Long id, Member memberDetails) {
        Member member = getMemberById(id);

        member.setUsername(memberDetails.getUsername());
        member.setEmail(memberDetails.getEmail());
        // Password update should be handled separately with encoding

        return memberRepository.save(member);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }
}
