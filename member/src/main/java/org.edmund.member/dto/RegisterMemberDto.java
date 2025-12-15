package org.edmund.member.dto;

import lombok.Data;

@Data
public class RegisterMemberDto {
    private String email;
    private String fullName;
    private String password;
}