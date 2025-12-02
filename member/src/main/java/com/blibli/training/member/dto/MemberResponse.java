package com.blibli.training.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private String username;
    private String email;
    private String token; // For login response
}
