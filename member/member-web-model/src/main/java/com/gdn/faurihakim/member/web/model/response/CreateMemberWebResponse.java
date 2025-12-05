package com.gdn.faurihakim.member.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMemberWebResponse {
    private String memberId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Long version;
    private Long createdDate;
    private String createdBy;
    private Long lastModifiedDate;
    private String lastModifiedBy;
    private boolean markForDelete;
}
