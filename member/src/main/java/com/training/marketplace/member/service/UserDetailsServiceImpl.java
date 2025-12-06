package com.training.marketplace.member.service;

import com.training.marketplace.member.entity.MemberEntity;
import com.training.marketplace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberEntity member = this.memberRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        return new User(
                member.getUsername(),
                member.getPassword(),
                getAuthority(member)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthority(MemberEntity member){
        GrantedAuthority authority = new SimpleGrantedAuthority(member.getRole());
        return List.of(authority);
    }
}
