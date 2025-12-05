package com.example.ecommerce.command;

import com.example.ecommerce.config.JwtUtils;
import com.example.ecommerce.dto.JwtResponse;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for authenticating a user and generating JWT token.
 */
@RequiredArgsConstructor
public class AuthenticateUserCommand implements Command<ResponseEntity<?>> {

        private final LoginRequest loginRequest;
        private final AuthenticationManager authenticationManager;
        private final JwtUtils jwtUtils;

        @Override
        public ResponseEntity<?> execute() {
                // Authenticate user credentials
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                loginRequest.getUsername(),
                                                loginRequest.getPassword()));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Generate JWT token
                String jwt = jwtUtils.generateJwtToken(authentication);

                // Extract user details
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities().stream()
                                .map(item -> item.getAuthority())
                                .collect(Collectors.toList());

                // Return JWT response
                return ResponseEntity.ok(new JwtResponse(
                                jwt,
                                userDetails.getId(),
                                userDetails.getUsername(),
                                userDetails.getEmail(),
                                roles));
        }
}
