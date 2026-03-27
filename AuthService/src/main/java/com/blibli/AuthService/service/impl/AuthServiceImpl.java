package com.blibli.AuthService.service.impl;

import com.blibli.AuthService.dto.LoginRequestDto;
import com.blibli.AuthService.dto.LoginResponseDto;
import com.blibli.AuthService.entity.RevokedToken;
import com.blibli.AuthService.entity.UserEntity;
import com.blibli.AuthService.exceptions.BadRequestException;
import com.blibli.AuthService.exceptions.UnauthorizedException;
import com.blibli.AuthService.repository.RevokedTokenRepository;
import com.blibli.AuthService.repository.UserRepository;
import com.blibli.AuthService.service.AuthService;
import com.blibli.AuthService.util.JwtUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RevokedTokenRepository revokedRepo;
    private final UserRepository userRepository;

    public AuthServiceImpl(AuthenticationManager authManager, JwtUtil jwtUtil, RevokedTokenRepository revokedRepo, UserRepository userRepository) {
        this.authenticationManager = authManager;
        this.jwtUtil = jwtUtil;
        this.revokedRepo = revokedRepo;
        this.userRepository = userRepository;
    }


    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        if (loginRequestDto == null) {
            throw new BadRequestException("Request body is missing");
        }

        if (loginRequestDto.getUsername() == null || loginRequestDto.getUsername().isBlank()) {
            throw new BadRequestException("Username must not be null or empty");
        }

        if (loginRequestDto.getPassword() == null || loginRequestDto.getPassword().isBlank()) {
            throw new BadRequestException("Password must not be null or empty");
        }

        try {
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()
                    );

            authenticationManager.authenticate(authentication);

        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid username or password");
        }
        UserEntity user = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        String token = jwtUtil.generateToken(user);
        long expiresAt = jwtUtil.getExpiry(token).getTime();


        return LoginResponseDto.builder()
                .message("Login successful")
                .token(token)
                .expiresIn(expiresAt)
                .userId(user.getId().toString())
                .build();
    }

    @Override
    public void revokeToken(String token) {
        if(!jwtUtil.validateToken(token))
            return;
        String jti = jwtUtil.getJti(token);
        Date expiry = jwtUtil.getExpiry(token);
        RevokedToken rt = new RevokedToken();
        rt.setJti(jti);
        rt.setExpiryEpochMillis(expiry.getTime());
        revokedRepo.save(rt);
    }

//    @Override
//    public boolean isRevoked(String jti) {
//        return revokedRepo.findById(jti).isPresent();
//    }

    @Scheduled(fixedDelayString = "PT10M")
    public void cleanup() {
        revokedRepo.deleteExpired(System.currentTimeMillis());
    }

}
