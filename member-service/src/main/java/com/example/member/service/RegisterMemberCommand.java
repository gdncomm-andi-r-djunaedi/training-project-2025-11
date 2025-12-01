package com.example.member.service;

import com.example.common.command.Command;
import com.example.member.domain.Member;
import com.example.member.exception.DuplicateEmailException;
import com.example.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class RegisterMemberCommand implements Command<Member> {

  private final MemberRepository repo;
  private final PasswordEncoder encoder;
  private final String email;
  private final String rawPassword;
  private final String fullName;

  @Override
  public Member execute() {
    repo.findByEmail(email).ifPresent(m -> {
      throw new DuplicateEmailException(email);
    });

    Member m = Member.builder()
        .email(email)
        .passwordHash(encoder.encode(rawPassword))
        .fullName(fullName)
        .build();
    return repo.save(m);
  }
}