package com.example.member.command;

import com.example.commandlib.Command;
import com.example.member.security.JwtUtil;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;

public class LoginCommand implements Command<ResponseEntity<?>> {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final String username;
    private final String password;

    public LoginCommand(AuthenticationManager authenticationManager, JwtUtil jwtUtil, 
                       String username, String password) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.username = username;
        this.password = password;
    }

    @Override
    public ResponseEntity<?> execute() {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        String token = jwtUtil.generateToken(username);
        ResponseCookie cookie = ResponseCookie.from("JWT-TOKEN", token).path("/").httpOnly(true).maxAge(24*60*60).build();
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(Map.of("token", token));
    }
}

