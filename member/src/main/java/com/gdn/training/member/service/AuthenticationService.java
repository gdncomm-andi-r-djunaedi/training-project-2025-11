package com.gdn.training.member.service;

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
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;

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

}
