package com.sc.memberservice.service;

import com.sc.memberservice.model.Member;
import com.sc.memberservice.repository.MemberRepository;
import com.sc.memberservice.utils.MemberPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public MyMemberDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isEmpty()) {
            throw new UsernameNotFoundException("Member not found with username: " + email);
        }

        return new MemberPrincipal(member.get());
    }
}
