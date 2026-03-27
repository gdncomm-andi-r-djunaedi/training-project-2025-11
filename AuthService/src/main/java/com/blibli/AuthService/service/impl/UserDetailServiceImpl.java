package com.blibli.AuthService.service.impl;

import com.blibli.AuthService.entity.UserEntity;
import com.blibli.AuthService.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {


    private final UserRepository repo;

    public UserDetailServiceImpl(UserRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity ue = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        return User.withUsername(ue.getUsername())
                .password(ue.getPassword())
                .roles("USER")
                .build();
    }
}
