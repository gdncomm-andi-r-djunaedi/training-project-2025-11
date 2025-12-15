package com.gdn.training.member.service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import com.gdn.training.member.model.request.AuthenticationRequest;
import com.gdn.training.member.model.response.AuthenticationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
        private final RedisTemplate<String, String> redisTemplate;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private static final String BLACKLIST_PREFIX = "blacklist:";

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                try {
                        var authToken = new UsernamePasswordAuthenticationToken(
                                        request.getEmail(),
                                        request.getPassword());

                        authenticationManager.authenticate(authToken);
                } catch (AuthenticationException ex) {
                        throw new IllegalArgumentException("Invalid email or password");
                }

                var token = jwtService.generateToken(request.getEmail());
                return new AuthenticationResponse(token);
        }

        public void logout(String token) {
                try {
                        Date expiration = jwtService.extractExpiration(token);
                        long ttl = expiration.getTime() - System.currentTimeMillis();

                        if (ttl > 0) {
                                String key = BLACKLIST_PREFIX + token;
                                redisTemplate.opsForValue().set(key, "revoked", ttl, TimeUnit.MILLISECONDS);
                        }
                } catch (Exception e) {
                        System.out.println("Token already invalid: " + e.getMessage());
                }
        }

}
