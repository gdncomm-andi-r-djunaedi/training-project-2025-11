package com.blublu.member.interfaces;

import com.blublu.member.model.request.LoginRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
  UserDetails authenticateUser(LoginRequest request);
}
