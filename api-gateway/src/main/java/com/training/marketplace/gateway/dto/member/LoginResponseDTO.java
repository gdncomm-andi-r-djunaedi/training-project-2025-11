package com.training.marketplace.gateway.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String memberId;
    private String authToken;
    private String refreshToken;
}
