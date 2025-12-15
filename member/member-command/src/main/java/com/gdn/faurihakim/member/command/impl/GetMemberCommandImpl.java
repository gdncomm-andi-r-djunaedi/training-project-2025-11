package com.gdn.faurihakim.member.command.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gdn.faurihakim.Member;
import com.gdn.faurihakim.MemberRepository;
import com.gdn.faurihakim.member.command.GetMemberCommand;
import com.gdn.faurihakim.member.command.model.GetMemberCommandRequest;
import com.gdn.faurihakim.member.model.MemberNotFoundException;
import com.gdn.faurihakim.member.web.model.response.GetMemberWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GetMemberCommandImpl implements GetMemberCommand {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CacheCommandImpl cacheService;

    @Override
    public GetMemberWebResponse execute(GetMemberCommandRequest request) {
        Member member = cacheService.get(request.getMemberId(), new TypeReference<>() {
        });
        if (member == null) {
            member = memberRepository.findByMemberId(request.getMemberId())
                    .orElseThrow(() -> new MemberNotFoundException("Member not found"));
            cacheService.set(request.getMemberId(), member);
        }

        GetMemberWebResponse response = new GetMemberWebResponse();
        BeanUtils.copyProperties(member, response);
        return response;
    }
}
