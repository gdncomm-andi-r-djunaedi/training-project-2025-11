package com.example.member.command;

import com.example.commandlib.Command;
import com.example.member.security.InMemoryTokenBlacklist;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class LogoutCommand implements Command<ResponseEntity<?>> {
    private final InMemoryTokenBlacklist tokenBlacklist;
    private final String authHeader;
    private final String cookieToken;

    public LogoutCommand(InMemoryTokenBlacklist tokenBlacklist, String authHeader, String cookieToken) {
        this.tokenBlacklist = tokenBlacklist;
        this.authHeader = authHeader;
        this.cookieToken = cookieToken;
    }

    @Override
    public ResponseEntity<?> execute() {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if (token == null) {
            token = cookieToken;
        }
        if (token != null) {
            tokenBlacklist.blacklist(token);
        }
        ResponseCookie cookie = ResponseCookie.from("JWT-TOKEN", "").path("/").httpOnly(true).maxAge(0).build();
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(Map.of("message", "logged out"));
    }
}

