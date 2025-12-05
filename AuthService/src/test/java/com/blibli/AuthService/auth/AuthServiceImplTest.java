package com.blibli.AuthService.auth;

import com.blibli.AuthService.dto.LoginRequestDto;
import com.blibli.AuthService.dto.LoginResponseDto;
import com.blibli.AuthService.entity.RevokedToken;
import com.blibli.AuthService.entity.UserEntity;
import com.blibli.AuthService.exceptions.BadRequestException;
import com.blibli.AuthService.exceptions.UnauthorizedException;
import com.blibli.AuthService.repository.RevokedTokenRepository;
import com.blibli.AuthService.repository.UserRepository;
import com.blibli.AuthService.service.impl.AuthServiceImpl;
import com.blibli.AuthService.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RevokedTokenRepository revokedRepo;


    @Test
    void login_shouldThrowBadRequest_whenNullRequest() {
        assertThrows(BadRequestException.class,
                () -> authService.login(null));
    }

    @Test
    void login_shouldThrowBadRequest_whenUsernameBlank() {
        LoginRequestDto dto = new LoginRequestDto("", "pass");

        assertThrows(BadRequestException.class,
                () -> authService.login(dto));
    }

    @Test
    void login_shouldThrowUnauthorized_whenBadCredentials() {
        LoginRequestDto dto = new LoginRequestDto("user", "wrong");

        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any(Authentication.class));

        assertThrows(UnauthorizedException.class,
                () -> authService.login(dto));
    }

    @Test
    void login_shouldReturnToken_whenSuccess() {
        LoginRequestDto dto = new LoginRequestDto("user", "pass");

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("user");

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");
        when(jwtUtil.getExpiry("jwt-token"))
                .thenReturn(new Date(System.currentTimeMillis() + 60000));

        LoginResponseDto response = authService.login(dto);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("1", response.getUserId());

        verify(authenticationManager)
                .authenticate(any(Authentication.class));
    }


    @Test
    void revokeToken_shouldSave_whenValid() {
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.getJti("token")).thenReturn("jti-123");
        when(jwtUtil.getExpiry("token"))
                .thenReturn(new Date(System.currentTimeMillis() + 10000));

        authService.revokeToken("token");

        verify(revokedRepo).save(any(RevokedToken.class));
    }

    @Test
    void revokeToken_shouldDoNothing_whenInvalid() {
        when(jwtUtil.validateToken("token")).thenReturn(false);

        authService.revokeToken("token");

        verifyNoInteractions(revokedRepo);
    }


    @Test
    void cleanup_shouldDeleteExpiredTokens() {
        authService.cleanup();

        verify(revokedRepo)
                .deleteExpired(anyLong());
    }
}
