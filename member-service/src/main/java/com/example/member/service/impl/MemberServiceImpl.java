package com.example.member.service.impl;

import com.example.common.command.CommandExecutor;
import com.example.member.command.RegisterMemberCommand;
import com.example.member.entity.Member;
import com.example.member.repository.MemberRepository;
import com.example.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRepository repo;
  private final PasswordEncoder encoder;
  private final CommandExecutor commandExecutor;

  @Override
  public Member register(String email, String rawPassword, String fullName) {
    // use Command pattern to encapsulate registration logic
    return commandExecutor.execute(
        new RegisterMemberCommand(repo, encoder, email, rawPassword, fullName)
    );
  }

  @Override
  public Member authenticate(String email, String rawPassword) {
    Member m = repo.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!encoder.matches(rawPassword, m.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    return m;
  }
}