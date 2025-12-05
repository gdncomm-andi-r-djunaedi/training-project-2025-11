package com.kailash.member.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private long expiresIn;
    private String refreshTokenId;
}
