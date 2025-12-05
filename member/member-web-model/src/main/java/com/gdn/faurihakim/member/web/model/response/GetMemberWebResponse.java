package com.gdn.faurihakim.member.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetMemberWebResponse {
    private String memberId;
    private String email;
    private String fullName;
    private String phoneNumber;
}
