package com.blibli.memberModule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponsedto {
    private String token;
    private String type;
    private Long expiresIn;
}
