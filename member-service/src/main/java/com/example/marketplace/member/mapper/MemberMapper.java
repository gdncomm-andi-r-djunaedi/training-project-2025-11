package com.example.marketplace.member.mapper;

import com.example.marketplace.member.domain.Member;
import com.example.marketplace.member.dto.MemberResponseDTO;

public class MemberMapper {
    public static MemberResponseDTO toDto(Member m) {
        return new MemberResponseDTO(m.getId(), m.getUsername(), m.getEmail());
    }
}
