package com.example.auth.memberservice.dto;

import com.example.auth.memberservice.model.Member;

public record LoginResponse(
    String id,
    String email,
    String role
) {
    public static LoginResponse fromMember(Member member) {
        return new LoginResponse(
                member.getId(),
                member.getEmail(),
                member.getRole()
        );
    }
}
