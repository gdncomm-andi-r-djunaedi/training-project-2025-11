package com.zasura.member.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MemberDetailResponse {
  private UUID id;
  private String name;
  private String email;
  private String phoneNumber;
}
