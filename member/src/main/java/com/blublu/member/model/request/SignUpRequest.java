package com.blublu.member.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequest {
  private String username;
  private String password;
}
