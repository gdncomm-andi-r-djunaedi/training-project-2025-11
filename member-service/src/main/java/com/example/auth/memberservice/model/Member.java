package com.example.auth.memberservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@Document(collection = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

  @Id
  private String id;

  @Indexed(unique = true)
  private String email;

  private String passwordHash;

  private String role;

  private Instant createdAt;
  private Instant updatedAt;
}