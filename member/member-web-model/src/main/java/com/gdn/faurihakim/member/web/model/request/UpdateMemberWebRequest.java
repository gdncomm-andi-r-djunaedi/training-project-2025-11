package com.gdn.faurihakim.member.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberWebRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
}