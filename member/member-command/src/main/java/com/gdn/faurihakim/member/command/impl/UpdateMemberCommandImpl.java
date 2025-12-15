package com.gdn.faurihakim.member.command.impl;

import com.gdn.faurihakim.Member;
import com.gdn.faurihakim.MemberRepository;
import com.gdn.faurihakim.member.command.UpdateMemberCommand;
import com.gdn.faurihakim.member.command.model.UpdateMemberCommandRequest;
import com.gdn.faurihakim.member.model.MemberNotFoundException;
import com.gdn.faurihakim.member.web.model.response.UpdateMemberWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UpdateMemberCommandImpl implements UpdateMemberCommand {
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public UpdateMemberWebResponse execute(UpdateMemberCommandRequest request) {
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + request.getMemberId()));
        if (member != null) {
            BeanUtils.copyProperties(request, member);
            memberRepository.save(member);
            UpdateMemberWebResponse response = new UpdateMemberWebResponse();
            BeanUtils.copyProperties(member, response);
            return response;
        } else {
            return null;
        }
    }
}
