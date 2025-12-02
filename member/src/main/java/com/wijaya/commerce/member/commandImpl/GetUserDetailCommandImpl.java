package com.wijaya.commerce.member.commandImpl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.command.GetUserDetailCommand;
import com.wijaya.commerce.member.commandImpl.model.GetUserDetailCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.GetUserDetailCommandResponse;
import com.wijaya.commerce.member.modelDb.MemberModelDb;
import com.wijaya.commerce.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetUserDetailCommandImpl implements GetUserDetailCommand {
    private final MemberRepository memberRepository;

    @Override
    public GetUserDetailCommandResponse doCommand(GetUserDetailCommandRequest request) {
        return findUserById(request.getId());
    }

    private GetUserDetailCommandResponse findUserById(String id) {
        Optional<MemberModelDb> memberModelDbOptional = memberRepository.findById(id);
        if (memberModelDbOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        MemberModelDb memberModelDb = memberModelDbOptional.get();
        return GetUserDetailCommandResponse.builder()
                .id(memberModelDb.getId())
                .email(memberModelDb.getEmail())
                .phoneNumber(memberModelDb.getPhoneNumber())
                .name(memberModelDb.getName())
                .status(memberModelDb.getStatus())
                .createdAt(memberModelDb.getCreatedAt())
                .updatedAt(memberModelDb.getUpdatedAt())
                .lastLoginAt(memberModelDb.getLastLoginAt())
                .build();
    }

}
