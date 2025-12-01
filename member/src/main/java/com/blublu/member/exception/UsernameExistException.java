package com.blublu.member.exception;

public class UsernameExistException extends RuntimeException{
  public UsernameExistException(String message) {
    super(message);
  }
}
