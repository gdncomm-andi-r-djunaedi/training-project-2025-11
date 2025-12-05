package com.gdn.training.member.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gdn.training.member.mapper.RegistrationMapper;
import com.gdn.training.member.model.entity.User;
import com.gdn.training.member.model.request.RegistrationRequest;
import com.gdn.training.member.repository.UserRepository;

import jakarta.validation.ValidationException;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegistrationMapper registrationMapper;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registerUserWithValidEmail_Success() {
        RegistrationRequest request = RegistrationRequest.builder()
                .email("test@example.com")
                .password("testing")
                .name("test user")
                .build();

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(request.getEmail());
        mockUser.setName(request.getName());
        mockUser.setPassword(passwordEncoder.encode(request.getPassword()));

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(registrationMapper.toUser(request)).thenReturn(mockUser);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        User result = registrationService.register(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUserWithExistingEmail_ThrowsValidationException() {
        RegistrationRequest request = RegistrationRequest.builder()
                .email("test@example.com")
                .password("testing")
                .name("test user")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            registrationService.register(request);
        });

        verify(userRepository, times(1)).existsByEmail(request.getEmail());

    }

}
