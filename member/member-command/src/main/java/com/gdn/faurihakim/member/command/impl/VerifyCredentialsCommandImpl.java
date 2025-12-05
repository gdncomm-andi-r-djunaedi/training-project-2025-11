package com.gdn.faurihakim.member.command.impl;

import com.gdn.faurihakim.Member;
import com.gdn.faurihakim.MemberRepository;
import com.gdn.faurihakim.member.command.VerifyCredentialsCommand;
import com.gdn.faurihakim.member.command.model.VerifyCredentialsCommandRequest;
import com.gdn.faurihakim.member.model.MemberNotFoundException;
import com.gdn.faurihakim.member.web.model.response.VerifyCredentialsWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VerifyCredentialsCommandImpl implements VerifyCredentialsCommand {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public VerifyCredentialsWebResponse execute(VerifyCredentialsCommandRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MemberNotFoundException("Member not found with email: " + request.getEmail()));

        if (passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            VerifyCredentialsWebResponse response = new VerifyCredentialsWebResponse();
            BeanUtils.copyProperties(member, response);
            return response;
        } else {
            throw new MemberNotFoundException("Invalid credentials");
        }
    }
}
