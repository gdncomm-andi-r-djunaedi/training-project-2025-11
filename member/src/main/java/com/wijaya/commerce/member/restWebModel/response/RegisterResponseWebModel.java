package com.wijaya.commerce.member.restWebModel.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponseWebModel {

    private String email;
    private String name;
    private String phoneNumber;
    private LocalDateTime createdAt;
}
