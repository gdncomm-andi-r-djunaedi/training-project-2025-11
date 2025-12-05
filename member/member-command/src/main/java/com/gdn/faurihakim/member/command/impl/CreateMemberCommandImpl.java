package com.gdn.faurihakim.member.command.impl;

import com.gdn.faurihakim.Member;
import com.gdn.faurihakim.MemberRepository;
import com.gdn.faurihakim.member.command.CreateMemberCommand;
import com.gdn.faurihakim.member.command.model.CreateMemberCommandRequest;
import com.gdn.faurihakim.member.web.model.response.CreateMemberWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class CreateMemberCommandImpl implements CreateMemberCommand {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public CreateMemberWebResponse execute(CreateMemberCommandRequest request) {
        Member member = new Member();
        BeanUtils.copyProperties(request, member);
        member.setMemberId(UUID.randomUUID().toString());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        Member savedMember = memberRepository.save(member);
        CreateMemberWebResponse response = new CreateMemberWebResponse();
        BeanUtils.copyProperties(savedMember, response);
        return response;
    }
}
