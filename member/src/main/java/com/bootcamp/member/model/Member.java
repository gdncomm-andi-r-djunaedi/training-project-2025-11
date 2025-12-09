package com.bootcamp.member.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

/**
 * Represents a member in the system.
 * This class holds basic member information including email and password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JsonIgnore
  private UUID id;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;
}
