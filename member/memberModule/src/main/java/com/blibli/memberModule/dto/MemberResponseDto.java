package com.blibli.memberModule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {

  private Long memberId;
  private String email;
  private String name;
  private String phone;

}

