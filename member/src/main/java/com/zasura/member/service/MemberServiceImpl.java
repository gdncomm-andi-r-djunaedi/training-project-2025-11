package com.zasura.member.service;

import com.zasura.member.dto.CreateMemberRequest;
import com.zasura.member.dto.LoginRequest;
import com.zasura.member.dto.LoginResponse;
import com.zasura.member.entity.Member;
import com.zasura.member.exception.AuthenticationFailedException;
import com.zasura.member.exception.EmailExistException;
import com.zasura.member.exception.MemberNotFoundException;
import com.zasura.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  @Override
  public boolean registerMember(CreateMemberRequest createMemberRequest) {
    if (memberRepository.existsByEmail(createMemberRequest.getEmail())) {
      throw new EmailExistException("Email is already in use!");
    }
    if (memberRepository.existsByPhoneNumber(createMemberRequest.getPhoneNumber())) {
      throw new EmailExistException("Phone Number is already in use!");
    }
    memberRepository.save(Member.builder()
        .name(createMemberRequest.getName())
        .phoneNumber(createMemberRequest.getPhoneNumber())
        .email(createMemberRequest.getEmail())
        .password(passwordEncoder.encode(createMemberRequest.getPassword()))
        .build());
    return true;
  }

  @Override
  public LoginResponse verifyMember(LoginRequest request) {
    Member member = memberRepository.findByEmail(request.getEmail());
    if (member == null) {
      throw new AuthenticationFailedException("Email Not Found!");
    }
    if (passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      return LoginResponse.builder()
          .uid(String.valueOf(member.getId()))
          .name(member.getName())
          .build();
    } else {
      throw new AuthenticationFailedException("Wrong Credentials!");
    }
  }

  @Override
  public Member getMember(UUID memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberNotFoundException("Member Not Found!"));
  }
}
