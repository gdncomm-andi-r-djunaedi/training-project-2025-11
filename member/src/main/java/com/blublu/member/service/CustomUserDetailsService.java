package com.blublu.member.service;

import com.blublu.member.entity.Member;
import com.blublu.member.exception.UsernameNotExistException;
import com.blublu.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  @Autowired
  private MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) {
    Member member = memberRepository.findByUsername(username);
    if (Objects.isNull(member))
      throw new UsernameNotExistException("User not found: " + username);

    return new User(member.getUsername(), member.getPassword(), List.of());
  }
}
