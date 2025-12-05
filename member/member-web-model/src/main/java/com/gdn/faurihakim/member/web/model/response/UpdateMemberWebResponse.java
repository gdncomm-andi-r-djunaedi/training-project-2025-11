package com.gdn.faurihakim.member.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberWebResponse {
    private String memberId;
    private String fullName;
    private String phoneNumber;
    private String email;
}