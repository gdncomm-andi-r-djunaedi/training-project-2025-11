package com.zasura.apiGateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailResponse {
  private Integer code;
  private String status;
  private Boolean success;
  private Member data;
  private Object errorMessage;
}
