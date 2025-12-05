package com.yuna.member.service.impl;

import com.yuna.member.model.User;
import com.yuna.member.repository.UserRepository;
import com.yuna.member.request.UserRequest;
import com.yuna.member.response.UserResponse;
import com.yuna.member.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  public UserResponse getUserByUsername (String username) {
    User user = userRepository.findByUsername(username).orElseThrow();
    return UserResponse.builder().email(user.getEmail()).name(user.getName()).username(user.getUsername()).build();
  }

  public UserResponse getUserByUsernameAndPassword (String username, String password) {
    User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
      throw new UsernameNotFoundException("User not found");
    }
    return UserResponse.builder().email(user.getEmail()).name(user.getName()).username(user.getUsername()).build();
  }

  @Override
  public UserResponse createUser(UserRequest request) {
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new RuntimeException("Username already exists");
    }
    User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .username(request.getUsername())
        .password(bCryptPasswordEncoder.encode(request.getPassword()))
        .build();
    userRepository.save(user);
    return UserResponse.builder().username(user.getUsername()).name(user.getName()).email(user.getEmail()).build();
  }
}
