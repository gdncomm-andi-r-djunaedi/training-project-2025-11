package com.zasura.apiGateway.exception;

public class MemberNotFoundException extends RuntimeException {
  public MemberNotFoundException(String message) {
    super(message);
  }
}
