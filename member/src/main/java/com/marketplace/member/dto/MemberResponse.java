package com.marketplace.member.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response DTO for member information.
 */
@Data
@Builder
public class MemberResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String address;
    private String phoneNumber;
}
