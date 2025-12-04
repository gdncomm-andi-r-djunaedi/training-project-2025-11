package com.marketplace.member.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MemberResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String address;
    private String phoneNumber;
}
