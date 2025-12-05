package com.gdn.faurihakim.member.command.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberCommandRequest {
    private String memberId;
    private String fullName;
    private String phoneNumber;
    private String email;
}
