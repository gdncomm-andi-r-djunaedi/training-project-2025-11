package com.wijaya.commerce.member.commandImpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterCommandResponse {
    private String email;
    private String name;
    private String phoneNumber;

}
