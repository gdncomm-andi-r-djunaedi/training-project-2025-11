package com.bootcamp.member.controller;

import com.bootcamp.member.model.Member;
import com.bootcamp.member.repository.MemberRepository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {
  private final MemberRepository memberRepository;

  public MemberController(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @RequestMapping(path = "/member", method = RequestMethod.GET)
  public Member getMember(String email) {
    return memberRepository.findByEmail(email);
  }

  @RequestMapping(path = "/member", method = RequestMethod.POST)
  public Member createMember(@RequestBody Member memberRequest) {
    return memberRepository.save(memberRequest);
  }
}
