package com.example.member.controller;

import com.example.commandlib.CommandExecutor;
import com.example.member.command.*;
import com.example.member.repo.UserRepository;
import com.example.member.security.InMemoryTokenBlacklist;
import com.example.member.security.JwtUtil;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final InMemoryTokenBlacklist tokenBlacklist;
    private final CommandExecutor commandExecutor = new CommandExecutor();

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil, InMemoryTokenBlacklist tokenBlacklist) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PreDestroy
    public void shutdown() {
        commandExecutor.shutdown();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq r) {
        return commandExecutor.execute(new RegisterCommand(
            userRepository, passwordEncoder, r.getUsername(), r.getPassword(), r.getFullname()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq r) {
        return commandExecutor.execute(new LoginCommand(
            authenticationManager, jwtUtil, r.getUsername(), r.getPassword()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader, @CookieValue(value = "JWT-TOKEN", required = false) String cookieToken) {
        return commandExecutor.execute(new LogoutCommand(tokenBlacklist, authHeader, cookieToken));
    }

    @Data 
    static class RegisterReq { 
        @NotBlank 
        private String username; 
        @NotBlank 
        private String password; 
        private String fullname;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullname() { return fullname; }
        public void setFullname(String fullname) { this.fullname = fullname; }
    }
    
    @Data 
    static class LoginReq { 
        @NotBlank 
        private String username; 
        @NotBlank 
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
