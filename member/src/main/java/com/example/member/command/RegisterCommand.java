package com.example.member.command;

import com.example.commandlib.Command;
import com.example.member.entity.User;
import com.example.member.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

public class RegisterCommand implements Command<ResponseEntity<?>> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String fullname;

    public RegisterCommand(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          String username, String password, String fullname) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
    }

    @Override
    public ResponseEntity<?> execute() {
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username already exists"));
        }
        User u = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullname(fullname)
                .build();
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }
}

