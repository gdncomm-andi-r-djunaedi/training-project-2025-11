package com.blublu.member.exception;

public class UsernameNotExistException extends RuntimeException{
  public UsernameNotExistException(String message) {
    super(message);
  }
}
