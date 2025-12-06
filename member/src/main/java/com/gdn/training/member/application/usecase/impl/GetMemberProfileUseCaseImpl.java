package com.gdn.training.member.application.usecase.impl;

import java.util.UUID;

import com.gdn.training.member.application.dto.response.MemberProfileResponse;
import com.gdn.training.member.application.usecase.GetMemberProfileUseCase;
import com.gdn.training.member.domain.port.out.MemberRepository;

/**
 * fetch member profile by member id
 * 
 * @author GDN Training
 * @version 1.0
 * @since 2025-12-02
 */
public class GetMemberProfileUseCaseImpl implements GetMemberProfileUseCase {

    private final MemberRepository memberRepository;

    public GetMemberProfileUseCaseImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberProfileResponse getProfile(UUID memberId) {
        if (memberRepository.findById(memberId).isEmpty()) {
            return null;
        }
        var member = memberRepository.findById(memberId).get();
        return new MemberProfileResponse(
                member.getId(),
                member.getFullName(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getAvatarUrl());
    }

}
