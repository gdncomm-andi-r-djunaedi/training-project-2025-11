package com.blibi.member.service;

import com.blibi.member.dto.MemberLoginRequest;
import com.blibi.member.dto.MemberRegisterRequest;
import com.blibi.member.dto.MemberResponse;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {
    MemberResponse register(MemberRegisterRequest request);
    MemberResponse login(MemberLoginRequest request);
}
