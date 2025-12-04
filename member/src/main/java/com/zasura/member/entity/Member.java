package com.zasura.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Table(name = "members")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @NotNull
  private String name;

  @NotBlank
  @NotNull
  @Email
  @Column(unique = true)
  private String email;

  @Pattern(regexp = "^[0-9]+$")
  @Column(unique = true)
  @NotNull
  @NotBlank
  private String phoneNumber;

  private String password;
}