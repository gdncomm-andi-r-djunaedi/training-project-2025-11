package com.kailash.member.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
}
