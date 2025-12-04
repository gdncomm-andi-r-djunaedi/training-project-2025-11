package com.zasura.apiGateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
  private UUID id;
  private String name;
  private String email;
  private String phoneNumber;
  private String password;
}