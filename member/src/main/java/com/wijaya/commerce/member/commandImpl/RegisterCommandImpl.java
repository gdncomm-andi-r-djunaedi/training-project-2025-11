package com.wijaya.commerce.member.commandImpl;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.command.RegisterCommand;
import com.wijaya.commerce.member.commandImpl.model.RegisterCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.RegisterCommandResponse;
import com.wijaya.commerce.member.modelDb.MemberModelDb;
import com.wijaya.commerce.member.repository.MemberRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RegisterCommandImpl implements RegisterCommand {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public RegisterCommandResponse doCommand(RegisterCommandRequest request) {
        Optional<MemberModelDb> account = memberRepository.findByEmail(request.getEmail());
        if (account.isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String passwordEncoded = passwordEncoder.encode(request.getPassword());
        MemberModelDb memberModelDb = MemberModelDb.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoded)
                .build();
        memberRepository.save(memberModelDb);
        return toRegisterCommandResponse(memberModelDb);
    }

    private RegisterCommandResponse toRegisterCommandResponse(MemberModelDb memberModelDb) {
        return RegisterCommandResponse.builder()
                .email(memberModelDb.getEmail())
                .name(memberModelDb.getName())
                .phoneNumber(memberModelDb.getPhoneNumber())
                .build();
    }

}
