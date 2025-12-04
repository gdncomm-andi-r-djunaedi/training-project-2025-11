package com.gdn.faurihakim.member.command.impl;

import com.gdn.faurihakim.Member;
import com.gdn.faurihakim.MemberRepository;
import com.gdn.faurihakim.member.command.GetMemberCommand;
import com.gdn.faurihakim.member.command.model.CreateMemberCommandRequest;
import com.gdn.faurihakim.member.command.model.GetMemberCommandRequest;
import com.gdn.faurihakim.member.web.model.response.CreateMemberWebResponse;
import com.gdn.faurihakim.member.web.model.response.GetMemberWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
public class GetMemberCommandImpl implements GetMemberCommand {
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public GetMemberWebResponse execute(GetMemberCommandRequest request) {
        Member member = memberRepository.findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        GetMemberWebResponse response = new GetMemberWebResponse();
        BeanUtils.copyProperties(member, response);
        return response;
    }
}
