package com.gdn.faurihakim.member.command.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMemberCommandRequest {
    private String memberId;
    @NotBlank(message = "NotBlank")
    private String email;

    @NotBlank(message = "NotBlank")
    private String password;

    private String fullName;

    @Pattern(message = "MustValid", regexp = "^$|^08[0-9]{8,12}$")
    private String phoneNumber;
}
