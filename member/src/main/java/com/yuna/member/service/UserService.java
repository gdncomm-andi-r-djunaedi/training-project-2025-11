package com.yuna.member.service;

import com.yuna.member.request.UserRequest;
import com.yuna.member.response.UserResponse;

public interface UserService {

  public UserResponse getUserByUsername (String username);

  public UserResponse createUser (UserRequest request);

  public UserResponse getUserByUsernameAndPassword (String username, String password);
}
