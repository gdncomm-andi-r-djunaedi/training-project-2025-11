package com.gdn.member.command;

import com.gdn.member.command.commandInterface.LoginMemberCommand;
import com.gdn.member.command.model.LoginMemberCommandRequest;
import com.gdn.member.controller.webmodel.response.LoginMemberResponse;
import com.gdn.member.entity.Member;
import com.gdn.member.exception.UnauthorizeException;
import com.gdn.member.repository.MemberRepository;
import com.gdn.member.utility.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginMemberCommandImpl implements LoginMemberCommand {

  private final MemberRepository memberRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtUtil jwtUtil;

  @Override
  public LoginMemberResponse execute(LoginMemberCommandRequest request) {
    Member member = memberRepository.findByUsername(request.getUsername())
      .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
      .orElseThrow(UnauthorizeException::new);

    String token = jwtUtil.generateToken(member.getId(), member.getUsername());
    return LoginMemberResponse.builder()
      .token(token)
      .build();
  }
}
