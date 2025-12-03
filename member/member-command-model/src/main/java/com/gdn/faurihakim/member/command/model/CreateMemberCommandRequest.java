package com.gdn.faurihakim.member.command.model;

import com.gdn.faurihakim.member.validation.MemberData;
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
public class CreateMemberCommandRequest implements MemberData {
    private String memberId;
    @NotBlank(message = "NotBlank")
    private String email;
    private String fullName;
    @Pattern(message = "MustValid", regexp = "^$|^08[0-9]{8,12}$")
    private String phoneNumber;
    @NotBlank(message = "NotBlank")
    private String password;
}
