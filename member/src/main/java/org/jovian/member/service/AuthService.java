package org.jovian.member.service;

import org.jovian.member.dto.LoginRequest;
import org.jovian.member.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest registerRequest);
    String login(LoginRequest loginRequest);
}
