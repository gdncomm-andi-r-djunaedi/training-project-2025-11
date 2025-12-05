package com.example.ecommerce.service;

import com.example.ecommerce.command.AuthenticateUserCommand;
import com.example.ecommerce.command.RegisterUserCommand;
import com.example.ecommerce.config.JwtUtils;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.SignupRequest;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations using Command Pattern.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtUtils jwtUtils;

        /**
         * Register a new user by executing RegisterUserCommand.
         * 
         * @param signUpRequest The signup request containing user details
         * @return ResponseEntity with success or error message
         */
        public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {
                RegisterUserCommand command = new RegisterUserCommand(
                                signUpRequest,
                                userRepository,
                                passwordEncoder);
                return command.execute();
        }

        /**
         * Authenticate a user by executing AuthenticateUserCommand.
         * 
         * @param loginRequest The login request containing credentials
         * @return ResponseEntity with JWT token and user details
         */
        public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
                AuthenticateUserCommand command = new AuthenticateUserCommand(
                                loginRequest,
                                authenticationManager,
                                jwtUtils);
                return command.execute();
        }
}
