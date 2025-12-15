package com.gdn.member.command;

import com.gdn.member.command.commandInterface.RegisterMemberCommand;
import com.gdn.member.command.model.RegisterMemberCommandRequest;
import com.gdn.member.controller.webmodel.response.RegisterMemberResponse;
import com.gdn.member.entity.Member;
import com.gdn.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterMemberCommandImpl implements RegisterMemberCommand {


  private final MemberRepository memberRepository;

  private final PasswordEncoder passwordEncoder;


  @Override
  public RegisterMemberResponse execute(RegisterMemberCommandRequest request) {
    Member member = memberRepository.save(Member.builder()
      .username(request.getUsername())
      .password(passwordEncoder.encode(request.getPassword()))
      .name(request.getName())
      .address(request.getAddress())
      .build());
    return RegisterMemberResponse.builder()
      .id(member.getId())
      .username(member.getUsername())
      .build();
  }
}
