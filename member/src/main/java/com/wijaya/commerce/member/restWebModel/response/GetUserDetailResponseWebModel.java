package com.wijaya.commerce.member.restWebModel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUserDetailResponseWebModel {
    private String id;
    private String email;
    private String phoneNumber;
    private String name;
    private String status;

}
