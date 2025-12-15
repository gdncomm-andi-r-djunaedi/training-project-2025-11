package com.yuna.member.controller;

import com.yuna.member.helper.HeaderHelper;
import com.yuna.member.request.UserRequest;
import com.yuna.member.response.UserResponse;
import com.yuna.member.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserServiceImpl userService;

  @GetMapping("/{username}")
  public UserResponse getUserByUsername(@PathVariable String username) {
    return userService.getUserByUsername(username);
  }

  @PostMapping
  public UserResponse createUser(@RequestBody UserRequest userRequest) {
    return userService.createUser(userRequest);
  }

  @GetMapping(value = "{username}", params = "password")
  public UserResponse getUserByUsernameAndPassword(@PathVariable String username, @RequestParam String password) {
    return userService.getUserByUsernameAndPassword(username, password);
  }

  @GetMapping(value = "/me")
  public UserResponse getCurrentUser(HttpServletRequest request) {
    String username = HeaderHelper.getUserName(request).orElseThrow(() -> new RuntimeException("header username empty"));
    return userService.getUserByUsername(username);
  }
}
