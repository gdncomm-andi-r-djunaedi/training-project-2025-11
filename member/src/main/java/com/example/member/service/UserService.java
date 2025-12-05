package com.example.member.service;

import com.example.member.model.User;

public interface UserService {
    User register(User user);
    User login(String email, String password);
}
