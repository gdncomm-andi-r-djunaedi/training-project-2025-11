package com.zasura.apiGateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VerifyMemberResponse {
  private String uid;
  private String name;
}
