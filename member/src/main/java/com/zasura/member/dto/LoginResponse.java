package com.zasura.member.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginResponse {
  private String uid;
  private String name;
}
