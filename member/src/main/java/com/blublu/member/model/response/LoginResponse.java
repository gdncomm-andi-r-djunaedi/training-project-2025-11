package com.blublu.member.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
  private String username;
  private Boolean success;
}
