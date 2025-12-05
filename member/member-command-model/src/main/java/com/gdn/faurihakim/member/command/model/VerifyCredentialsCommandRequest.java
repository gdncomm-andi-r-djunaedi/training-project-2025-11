package com.gdn.faurihakim.member.command.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCredentialsCommandRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
