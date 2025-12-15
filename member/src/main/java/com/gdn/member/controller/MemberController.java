package com.gdn.member.controller;

import com.gdn.member.command.commandInterface.LoginMemberCommand;
import com.gdn.member.command.commandInterface.RegisterMemberCommand;
import com.gdn.member.command.model.LoginMemberCommandRequest;
import com.gdn.member.command.model.RegisterMemberCommandRequest;
import com.gdn.member.controller.base.BaseCommandController;
import com.gdn.member.controller.webmodel.request.LoginMemberRequest;
import com.gdn.member.controller.webmodel.request.RegisterMemberRequest;
import com.gdn.member.controller.webmodel.response.LoginMemberResponse;
import com.gdn.member.controller.webmodel.response.RegisterMemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class MemberController extends BaseCommandController {

  @PostMapping("/members/register")
  public ResponseEntity<RegisterMemberResponse> registerMember(@Valid @RequestBody RegisterMemberRequest request){
    return ResponseEntity.status(HttpStatus.CREATED).body(executor.execute(
            RegisterMemberCommand.class,
            RegisterMemberCommandRequest.builder()
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .name(request.getName())
                    .address(request.getAddress())
                    .build()));
  }

  @PostMapping("/auth/login")
  public ResponseEntity<LoginMemberResponse> login(@Valid @RequestBody LoginMemberRequest request) {
    return ResponseEntity.ok(executor.execute(
      LoginMemberCommand.class,
      LoginMemberCommandRequest.builder()
        .username(request.getUsername())
        .password(request.getPassword())
        .build()
    ));
  }

}
