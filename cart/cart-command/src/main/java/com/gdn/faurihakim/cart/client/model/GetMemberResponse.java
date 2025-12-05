package com.gdn.faurihakim.cart.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetMemberResponse {
    private String memberId;
    private String email;
    private String fullName;
    private String phoneNumber;
}
