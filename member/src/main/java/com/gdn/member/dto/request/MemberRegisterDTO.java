package com.gdn.member.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberRegisterDTO {
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
}
