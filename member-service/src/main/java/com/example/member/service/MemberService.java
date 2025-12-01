package com.example.member.service;

import com.example.member.entity.Member;

public interface MemberService {

  Member register(String email, String rawPassword, String fullName);

  /**
   * Validate credentials and return the member.
   * Throws IllegalArgumentException if invalid.
   */
  Member authenticate(String email, String rawPassword);
}
