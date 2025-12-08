package com.blibi.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberRegisterRequest {
    private String userName;
    private String password;
    private String email;
}
