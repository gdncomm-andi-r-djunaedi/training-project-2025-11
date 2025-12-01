package com.example.member.exception;

import lombok.Getter;

@Getter
public class DuplicateEmailException extends RuntimeException {

  private final String email;

  public DuplicateEmailException(String email) {
    super("Email already registered: " + email);
    this.email = email;
  }
}
